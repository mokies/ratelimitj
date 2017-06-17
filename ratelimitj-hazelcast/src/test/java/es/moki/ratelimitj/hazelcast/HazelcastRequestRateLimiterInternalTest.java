package es.moki.ratelimitj.hazelcast;


import com.google.common.collect.ImmutableSet;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.test.time.TimeBanditSupplier;
import es.moki.ratelimitj.core.time.TimeSupplier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class HazelcastRequestRateLimiterInternalTest {

    private static HazelcastInstance hz;

    private final TimeBanditSupplier timeBandit = new TimeBanditSupplier();

    @BeforeAll
    public static void before() {
        hz = Hazelcast.newHazelcastInstance();
    }

    @AfterAll
    public static void after() {
        hz.shutdown();
    }

    private RequestRateLimiter getRateLimiter(Set<RequestLimitRule> rules, TimeSupplier timeSupplier) {
        return new HazelcastSlidingWindowRequestRateLimiter(hz, rules, timeSupplier);
    }

    @Test
    public void shouldEventuallyCleanUpExpiredKeys() throws Exception {
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

}
