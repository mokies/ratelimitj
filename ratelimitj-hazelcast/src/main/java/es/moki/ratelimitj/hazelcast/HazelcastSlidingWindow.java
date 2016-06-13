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

    // TODO support muli keys
    public boolean isOverLimit(String key, Set<LimitRule> rules, int weight) {

        // TODO assert must have at least one rule
        requireNonNull(key, "key cannot be null");
        requireNonNull(rules, "rules cannot be null");
        if (rules.isEmpty()) {
            throw new IllegalArgumentException("at least one rule must be provided");
        }

        long now = System.currentTimeMillis();
        long longestDurection = rules.stream().findFirst().get().getDurationSeconds();
        List<SavedKey> savedKeyKeys = new ArrayList<>();

        ConcurrentMap<String, Long> hcKeyMap = hc.getMap(key);

        // TODO perform each rule calculation in parallel
        for (LimitRule rule : rules) {
            int duration = rule.getDurationSeconds();
            longestDurection = Math.max(longestDurection, duration);
            int precision = rule.getPrecision().orElse(duration);
            precision = Math.min(precision, duration);
            long blocks = (long) Math.ceil(duration / precision);

            SavedKey savedKey = new SavedKey();
            savedKey.blockId = (long) Math.floor(now / precision);
            savedKey.trimBefore = savedKey.blockId - blocks + 1;
            savedKey.countKey = "" + duration + ':' + precision + ':';
            savedKey.tsKey = savedKey.countKey + 'o';
            savedKeyKeys.add(savedKey);

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
                cur = hcKeyMap.computeIfPresent(savedKey.countKey, (k, v) -> v - decrement);
            } else {
                // cur = redis.call('HGET', key, saved.count_key)
                cur = hcKeyMap.get(savedKey.countKey);
            }

            // check our limits
            if (Optional.ofNullable(cur).orElse(0L) + weight > rule.getLimit()) {
                return true;
            }
        }

        // there is enough resources, update the counts
        for (SavedKey savedKey : savedKeyKeys) {
            //for (
            //update the current timestamp, count, and bucket count
                //hcKeyMap.put(savedKey.tsKey, savedKey.trimBefore);

        }

//        for i, limit in ipairs(limits) do
//            local saved = saved_keys[i]
//            for j, key in ipairs(KEYS) do
//                -- update the current timestamp, count, and bucket count
//                redis.call('HSET', key, saved.ts_key, saved.trim_before)
//                redis.call('HINCRBY', key, saved.count_key, weight)
//                redis.call('HINCRBY', key, saved.count_key .. saved.block_id, weight)
//            end
//        end

        return false;
    }


    private static class SavedKey {

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
