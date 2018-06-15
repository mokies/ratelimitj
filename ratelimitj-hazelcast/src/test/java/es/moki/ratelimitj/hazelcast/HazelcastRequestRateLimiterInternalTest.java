package es.moki.ratelimitj.hazelcast;


import com.google.common.collect.ImmutableSet;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.test.time.TimeBanditSupplier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static es.moki.ratelimitj.hazelcast.HazelcastTestFactory.newStandaloneHazelcastInstance;
import static org.assertj.core.api.Assertions.assertThat;

class HazelcastRequestRateLimiterInternalTest {

    private static HazelcastInstance hz;

    private final TimeBanditSupplier timeBandit = new TimeBanditSupplier();

    @BeforeAll
    static void beforeAll() {
        hz = newStandaloneHazelcastInstance();
    }

    @AfterAll
    static void afterAll() {
        hz.shutdown();
    }

    @AfterEach
    void afterEach() {
        hz.getDistributedObjects().forEach(DistributedObject::destroy);
    }

    private RequestRateLimiter getRateLimiter(Set<RequestLimitRule> rules, TimeSupplier timeSupplier) {
        return new HazelcastSlidingWindowRequestRateLimiter(hz, rules, timeSupplier);
    }

    @Test
    void shouldEventuallyCleanUpExpiredKeys() throws Exception {
        ImmutableSet<RequestLimitRule> rules = ImmutableSet.of(RequestLimitRule.of(2, TimeUnit.SECONDS, 5));
        RequestRateLimiter requestRateLimiter = getRateLimiter(rules, timeBandit);

        String key = "ip:127.0.0.5";

        IntStream.rangeClosed(1, 5).forEach(value -> {
            timeBandit.addUnixTimeMilliSeconds(100L);
            assertThat(requestRateLimiter.overLimitWhenIncremented(key)).isFalse();
        });

        IMap<Object, Object> map = hz.getMap(key);
        
        while (map.size() != 0) {
            Thread.sleep(10);
        }
        assertThat(map.size()).isZero();
    }

    
    @Test
    void shouldCheckIncrementAnyway() throws Exception {
    	
        ImmutableSet<RequestLimitRule> rules = ImmutableSet.of(RequestLimitRule.of(20, TimeUnit.SECONDS, 5));
        RequestRateLimiter requestRateLimiter = getRateLimiter(rules, timeBandit);

        String key = "ip:127.0.0.5";

        timeBandit.addUnixTimeMilliSeconds(100L);
        // Counter should be 0 now
        assertThat(requestRateLimiter.overLimitWhenIncremented(key, 1)).isFalse();
        // Counter should be 1 now and 'false', meaning not over limit
        
        timeBandit.addUnixTimeMilliSeconds(100L);
        assertThat(requestRateLimiter.incrementRegardless(key, 10)).isTrue();
        // Counter should be 11 now and 'true', meaning over limit
        
        timeBandit.addUnixTimeMilliSeconds(100L);
        assertThat(requestRateLimiter.overLimitWhenIncremented(key, 1)).isTrue();
        // Counter should be 11 now and 'true', meaning over limit
        // If the increment regardless worked, then the value would have been 2
        // which would not be under the limit.
        
        
        
        
    }
    
    
}
