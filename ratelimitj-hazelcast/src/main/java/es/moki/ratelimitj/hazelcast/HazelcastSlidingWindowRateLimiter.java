package es.moki.ratelimitj.hazelcast;

import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import es.moki.ratelimitj.core.api.LimitRule;
import es.moki.ratelimitj.core.api.RateLimiter;
import es.moki.ratelimitj.core.time.SystemTimeSupplier;
import es.moki.ratelimitj.core.time.TimeSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@ThreadSafe
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

    @Override
    public boolean overLimit(String key) {
        return overLimit(key, 1);
    }

    // TODO support muli keys
    @Override
    public boolean overLimit(String key, int weight) {

        requireNonNull(key, "key cannot be null");
        requireNonNull(rules, "rules cannot be null");
        if (rules.isEmpty()) {
            throw new IllegalArgumentException("at least one rule must be provided");
        }

        final long now = timeSupplier.get();
        // TODO implement cleanup
        final int longestDuration = rules.stream().map(LimitRule::getDurationSeconds).reduce(Integer::max).orElse(0);
        List<SavedKey> savedKeys = new ArrayList<>(rules.size());

        IMap<String, Long> hcKeyMap = getMap(key, longestDuration);

        // TODO perform each rule calculation in parallel
        for (LimitRule rule : rules) {

            SavedKey savedKey = new SavedKey(now, rule.getDurationSeconds(), rule.getPrecision());
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
            long trim = Math.min(savedKey.trimBefore, oldTs + savedKey.blocks);

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
//                dele.stream().map(hcKeyMap::removeAsync).collect(Collectors.toList());
                dele.forEach(hcKeyMap::remove);
                final long decrement = decr;
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
            hcKeyMap.set(savedKey.tsKey, savedKey.trimBefore);
            // TODO should this ben just compute
            Long computedCountKeyValue = hcKeyMap.compute(savedKey.countKey, (k, v) -> Optional.ofNullable(v).orElse(0L) + weight);
            LOG.debug("{} {}={}", key, savedKey.countKey, computedCountKeyValue);
            Long computedCountKeyBlockIdValue = hcKeyMap.compute(savedKey.countKey + savedKey.blockId, (k, v) -> Optional.ofNullable(v).orElse(0L) + weight);
            LOG.debug("{} {}={}", key, savedKey.countKey + savedKey.blockId, computedCountKeyBlockIdValue);

        }

        return false;
    }

    @Override
    public boolean resetLimit(String key) {
        throw new RuntimeException("Not implemented");
    }

    private IMap<String, Long> getMap(String key, int longestDuration) {

        MapConfig mapConfig = hz.getConfig().getMapConfig(key);
        mapConfig.setTimeToLiveSeconds(longestDuration);
        mapConfig.setAsyncBackupCount(1);
        mapConfig.setBackupCount(0);
        return hz.getMap(key);
    }

    private static class SavedKey {
        final long blockId;
        final long blocks;
        final long trimBefore;
        final String countKey;
        final String tsKey;

        SavedKey(long now, int duration, OptionalInt precisionOpt) {

            int precision = precisionOpt.orElse(duration);
            precision = Math.min(precision, duration);

            this.blocks = (long) Math.ceil(duration / (double) precision);
            this.blockId = (long) Math.floor(now / (double) precision);
            this.trimBefore = blockId - blocks + 1;
            this.countKey = "" + duration + ':' + precision + ':';
            this.tsKey = countKey + 'o';
        }
    }
}
