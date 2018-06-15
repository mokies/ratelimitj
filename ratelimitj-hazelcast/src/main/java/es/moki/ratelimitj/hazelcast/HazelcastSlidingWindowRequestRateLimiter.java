package es.moki.ratelimitj.hazelcast;

import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.core.time.SystemTimeSupplier;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.inmemory.request.SavedKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static es.moki.ratelimitj.core.RateLimitUtils.coalesce;
import static java.util.Objects.requireNonNull;

@ThreadSafe
public class HazelcastSlidingWindowRequestRateLimiter implements RequestRateLimiter {

    private static final Logger LOG = LoggerFactory.getLogger(HazelcastSlidingWindowRequestRateLimiter.class);

    private final HazelcastInstance hz;
    private final Set<RequestLimitRule> rules;
    private final TimeSupplier timeSupplier;

    public HazelcastSlidingWindowRequestRateLimiter(HazelcastInstance hz, Set<RequestLimitRule> rules) {
        this(hz, rules, new SystemTimeSupplier());
    }

    public HazelcastSlidingWindowRequestRateLimiter(HazelcastInstance hz, Set<RequestLimitRule> rules, TimeSupplier timeSupplier) {
        requireNonNull(hz, "hazelcast can not be null");
        requireNonNull(rules, "rules can not be null");
        requireNonNull(rules, "time supplier can not be null");
        this.hz = hz;
        this.rules = rules;
        this.timeSupplier = timeSupplier;
    }

    @Override
    public boolean overLimitWhenIncremented(String key) {
        return overLimitWhenIncremented(key, 1);
    }

    // TODO support muli keys
    @Override
    public boolean overLimitWhenIncremented(String key, int weight) {
        return eqOrGeLimit(key, weight, true, false);
    }

    @Override
    public boolean geLimitWhenIncremented(String key) {
        return geLimitWhenIncremented(key, 1);
    }

    @Override
    public boolean geLimitWhenIncremented(String key, int weight) {
        return eqOrGeLimit(key, weight, false, false);
    }
    
    
    
    @Override
	public boolean incrementRegardless(String key, int weight) {
    	    return eqOrGeLimit(key, weight, false, true);
	}
    
    

	@Override
    public boolean resetLimit(String key) {
        IMap<Object, Object> map = hz.getMap(key);
        if (map == null || map.isEmpty()) {
            return false;
        }
        map.clear();
        map.destroy();
        return true;
    }

    private IMap<String, Long> getMap(String key, int longestDuration) {
        MapConfig mapConfig = hz.getConfig().getMapConfig(key);
        mapConfig.setTimeToLiveSeconds(longestDuration);
        mapConfig.setAsyncBackupCount(1);
        mapConfig.setBackupCount(0);
        return hz.getMap(key);
    }

    private boolean eqOrGeLimit(String key, int weight, boolean strictlyGreater, boolean increment_anyway) {

        requireNonNull(key, "key cannot be null");
        requireNonNull(rules, "rules cannot be null");
        if (rules.isEmpty()) {
            throw new IllegalArgumentException("at least one rule must be provided");
        }

        final long now = timeSupplier.get();
        // TODO implement cleanup
        final int longestDuration = rules.stream().map(RequestLimitRule::getDurationSeconds).reduce(Integer::max).orElse(0);
        List<SavedKey> savedKeys = new ArrayList<>(rules.size());

        IMap<String, Long> hcKeyMap = getMap(key, longestDuration);
        boolean geLimit = false;

        // TODO perform each rule calculation in parallel
        for (RequestLimitRule rule : rules) {

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
            long count = coalesce(cur, 0L) + weight;
            if (count > rule.getLimit()) {
            	    geLimit = true;
            	    if (!increment_anyway) {
                    return true; // over limit, don't record request
            	    }
            } else if (!strictlyGreater && count == rule.getLimit()) {
                geLimit = true; // at limit, do record request
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

        return geLimit;
    }
}
