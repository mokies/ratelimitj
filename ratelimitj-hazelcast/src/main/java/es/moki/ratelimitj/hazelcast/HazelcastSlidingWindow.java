package es.moki.ratelimitj.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import es.moki.ratelimitj.core.LimitRule;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;


public class HazelcastSlidingWindow {

    private final HazelcastInstance hc;

    public HazelcastSlidingWindow(HazelcastInstance hc) {
        this.hc = hc;
    }

    public boolean isOverLimit(String key, Set<LimitRule> rules, int weight) {

        // TODO assert must have at least one rule

        long now = System.currentTimeMillis();
        long longestDurection = rules.stream().findFirst().get().getDurationSeconds();
        Queue<Saved> savedKeys = new LinkedList<>();

        for (LimitRule rule: rules) {
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

            ConcurrentMap<String, String> map = hc.getMap("my-distributed-map");
            String oldTs = map.get(saved.tsKey) ;

//            oldTsLong = oldTs and tonumber(oldTs) or saved.trimBefore;
            long oldTsLong = oldTs != null ? Long.parseLong(oldTs) : saved.trimBefore;

            if (oldTsLong > now) {
                return true;
            }

        }

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
