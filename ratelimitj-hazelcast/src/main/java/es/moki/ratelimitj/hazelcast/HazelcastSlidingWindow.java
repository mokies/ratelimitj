package es.moki.ratelimitj.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import es.moki.ratelimitj.core.LimitRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static java.util.Objects.requireNonNull;


public class HazelcastSlidingWindow {

    private final HazelcastInstance hc;

    public HazelcastSlidingWindow(HazelcastInstance hc) {
        this.hc = hc;
    }

    public boolean isOverLimit(String key, Set<LimitRule> rules, int weight) {

        // TODO assert must have at least one rule
        requireNonNull(key, "key cannot be null");
        requireNonNull(rules, "rules cannot be null");
        if (rules.isEmpty()) {
            throw new IllegalArgumentException("at least one rule must be provided");
        }

        long now = System.currentTimeMillis();
        long longestDurection = rules.stream().findFirst().get().getDurationSeconds();
        List<Saved> savedKeys = new ArrayList<>();

        // TODO perform each rule calculation in parallel
        for (LimitRule rule : rules) {
            int duration = rule.getDurationSeconds();
            longestDurection = Math.max(longestDurection, duration);
            int precision = rule.getPrecision().orElse(duration);
            precision = Math.min(precision, duration);
            long blocks = (long) Math.ceil(duration / precision);

            Saved saved = new Saved();
            saved.blockId = (long) Math.floor(now / precision);
            saved.trimBefore = saved.blockId - blocks + 1;
            saved.countKey = "" + duration + ':' + precision + ':';
            saved.tsKey = saved.countKey + 'o';
            savedKeys.add(saved);

            ConcurrentMap<String, Long> hcKeyMap = hc.getMap(key);
            Long oldTs = hcKeyMap.get(saved.tsKey);

            //oldTs = Optional.ofNullable(oldTs).orElse(saved.trimBefore);
            oldTs = oldTs != null ? oldTs : saved.trimBefore;

            if (oldTs > now) {
                // don't write in the past
                return true;
            }

            // discover what needs to be cleaned up
            long decr = 0;
            List<String> dele = new ArrayList<>();
            long trim = Math.min(saved.trimBefore, oldTs + blocks);
            // TODO suspect I have an off by one error here
            for (long oldBlock = oldTs; oldBlock == trim - 1; oldBlock++) {
                String bkey = saved.countKey + oldBlock;
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
                cur = hcKeyMap.computeIfPresent(saved.countKey, (k, v) -> v - decrement);
            } else {
                // cur = redis.call('HGET', key, saved.count_key)
                cur = hcKeyMap.get(saved.countKey);
            }

            // check our limits
            if (Optional.ofNullable(cur).orElse(0L) + weight > rule.getLimit()) {
                return true;
            }
        }

        // there is enough resources, update the counts

        return false;
    }


    private static class Saved {

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
