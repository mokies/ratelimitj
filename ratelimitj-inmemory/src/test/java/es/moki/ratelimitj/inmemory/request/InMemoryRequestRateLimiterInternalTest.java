package es.moki.ratelimitj.inmemory.request;


import com.google.common.collect.ImmutableSet;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.test.time.TimeBanditSupplier;
import net.jodah.expiringmap.ExpiringMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryRequestRateLimiterInternalTest {


    private final TimeBanditSupplier timeBandit = new TimeBanditSupplier();

    private final ExpiringMap expiryingKeyMap = ExpiringMap.builder().variableExpiration().build();

    @BeforeAll
    static void before() {

    }

    @AfterAll
    static void after() {

    }

    private RequestRateLimiter getRateLimiter(Set<RequestLimitRule> rules, TimeSupplier timeSupplier) {
        return new InMemorySlidingWindowRequestRateLimiter(expiryingKeyMap, rules, timeSupplier);
    }

    @Test
    @Disabled
    void shouldEventuallyCleanUpExpiredKeys() throws Exception {
        ImmutableSet<RequestLimitRule> rules = ImmutableSet.of(RequestLimitRule.of(1, TimeUnit.SECONDS, 5));
        RequestRateLimiter requestRateLimiter = getRateLimiter(rules, timeBandit);

        String key = "ip:127.0.0.5";

        IntStream.rangeClosed(1, 5).forEach(value -> {
            timeBandit.addUnixTimeMilliSeconds(100L);
            assertThat(requestRateLimiter.overLimitWhenIncremented(key)).isFalse();
        });

        while (expiryingKeyMap.size() != 0) {
            Thread.sleep(50);
        }

        assertThat(expiryingKeyMap.size()).isZero();
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
