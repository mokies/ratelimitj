package es.moki.ratelimitj.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import es.moki.ratelimitj.api.LimitRule;
import es.moki.ratelimitj.api.RateLimiter;
import es.moki.ratelimitj.core.time.time.SystemTimeSupplier;
import es.moki.ratelimitj.core.time.time.TimeSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static java.util.Objects.requireNonNull;


public class HazelcastSlidingWindowRateLimiter implements RateLimiter {

    private static final Logger LOG = LoggerFactory.getLogger(HazelcastSlidingWindowRateLimiter.class);

    private final HazelcastInstance hz;
    private final Set<LimitRule> rules;
    private final TimeSupplier timeSupplier;

    public HazelcastSlidingWindowRateLimiter(HazelcastInstance hz, Set<LimitRule> rules) {
        this(hz, rules, new SystemTimeSupplier());
    }

    public HazelcastSlidingWindowRateLimiter(HazelcastInstance hz, Set<LimitRule> rules, TimeSupplier timeSupplier) {
        this.hz = hz;
        this.rules = rules;
        this.timeSupplier = timeSupplier;
    }

    // TODO support muli keys
    @Override
    public boolean overLimit(String key, int weight) {

        // TODO assert must have at least one rule
        requireNonNull(key, "key cannot be null");
        requireNonNull(rules, "rules cannot be null");
        if (rules.isEmpty()) {
            throw new IllegalArgumentException("at least one rule must be provided");
        }

        final long now = timeSupplier.get();
        final long longestDuration = rules.stream().map(LimitRule::getDurationSeconds).reduce(Integer::max).get();
        List<SavedKey> savedKeys = new ArrayList<>(rules.size());

        ConcurrentMap<String, Long> hcKeyMap = hz.getMap(key);

        // TODO perform each rule calculation in parallel
        for (LimitRule rule : rules) {
            int duration = rule.getDurationSeconds();
            int precision = rule.getPrecision().orElse(duration);
            precision = Math.min(precision, duration);
            long blocks = (long) Math.ceil(duration / precision);

            SavedKey savedKey = new SavedKey();
            savedKey.blockId = (long) Math.floor(now / precision);
            savedKey.trimBefore = savedKey.blockId - blocks + 1;
            savedKey.countKey = "" + duration + ':' + precision + ':';
            savedKey.tsKey = savedKey.countKey + 'o';
            savedKeys.add(savedKey);

            Long oldTs = hcKeyMap.get(savedKey.tsKey);

            //oldTs = Optional.ofNullable(oldTs).orElse(saved.trimBefore);
            oldTs = oldTs != null ? oldTs : savedKey.trimBefore;

            if (oldTs > now) {
                // don't write in the past
                return true;
            }

            // discover what needs to be cleaned up
            long decr = 0;
            List<String> dele = new ArrayList<>();
            long trim = Math.min(savedKey.trimBefore, oldTs + blocks);

            // TODO suspect I have an off by one error here
            for (long oldBlock = oldTs; oldBlock == trim - 1; oldBlock++) {
                String bkey = savedKey.countKey + oldBlock;
                Long bcount = hcKeyMap.get(bkey);
                if (bcount != null) {
                    decr = decr + bcount;
                    dele.add(bkey);
                }
            }

            // handle cleanup
            Long cur;
            if (!dele.isEmpty()) {
                dele.stream().forEach(hcKeyMap::remove);
                final long decrement = decr;
                // TODO should this ben just compute
                cur = hcKeyMap.compute(savedKey.countKey, (k, v) -> v - decrement);
            } else {
                cur = hcKeyMap.get(savedKey.countKey);
            }

            // check our limits
            if (Optional.ofNullable(cur).orElse(0L) + weight > rule.getLimit()) {
                return true;
            }
        }

        // there is enough resources, update the counts
        for (SavedKey savedKey : savedKeys) {
            //update the current timestamp, count, and bucket count
            hcKeyMap.put(savedKey.tsKey, savedKey.trimBefore);
            // TODO should this ben just compute
            Long computedCountKeyValue = hcKeyMap.compute(savedKey.countKey, (k, v) -> Optional.ofNullable(v).orElse(0L) + weight);
            //LOG.debug("{}={}", savedKey.countKey, computedCountKeyValue);
            Long computedCountKeyBlockIdValue = hcKeyMap.compute(savedKey.countKey + savedKey.blockId, (k, v) -> Optional.ofNullable(v).orElse(0L) + weight);
            //LOG.debug("{}={}", savedKey.countKey + savedKey.blockId, computedCountKeyValue);

        }

        // We calculated the longest-duration limit so we can EXPIRE
//        if (longestDuration > 0) {
            //hcKeyMap.remove(longestDuration);
            // TODO this map will grow indefinitely, need to find a way to evict stale keys.
//            hz.getMap(key).
//        }

        return false;
    }

    @Override public boolean overLimit(String key) {
        return overLimit(key, 1);
    }


    private static class SavedKey {

        public SavedKey() { }
//        public SavedKey(long now, LimitRule rule) {
//            blockId = (long) Math.floor(now / precision);
//            trimBefore = savedKey.blockId - blocks + 1;
//            countKey = "" + duration + ':' + precision + ':';
//            tsKey = savedKey.countKey + 'o';
//        }

        long blockId;
        long trimBefore;
        String countKey;
        String tsKey;
//        saved.block_id = Math.floor(now / precision)
//        saved.trim_before = saved.block_id - blocks + 1
//        saved.count_key = duration .. ':' .. precision .. ':'
//        saved.ts_key = saved.count_key .. 'o'

    }
}
