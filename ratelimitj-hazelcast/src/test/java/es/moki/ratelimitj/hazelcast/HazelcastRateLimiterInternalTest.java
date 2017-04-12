package es.moki.ratelimitj.hazelcast;


import com.google.common.collect.ImmutableSet;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import es.moki.ratelimitj.core.api.LimitRule;
import es.moki.ratelimitj.core.api.RateLimiter;
import es.moki.ratelimitj.core.time.TimeBanditSupplier;
import es.moki.ratelimitj.core.time.TimeSupplier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class HazelcastRateLimiterInternalTest {

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

    private RateLimiter getRateLimiter(Set<LimitRule> rules, TimeSupplier timeSupplier) {
        return new HazelcastSlidingWindowRateLimiter(hz, rules, timeSupplier);
    }

    @Test
    public void shouldEventuallyCleanUpExpiredKeys() throws Exception {
        ImmutableSet<LimitRule> rules = ImmutableSet.of(LimitRule.of(2, TimeUnit.SECONDS, 5));
        RateLimiter rateLimiter = getRateLimiter(rules, timeBandit);

        String key = "ip:127.0.0.5";

        IntStream.rangeClosed(1, 5).forEach(value -> {
            timeBandit.addUnixTimeMilliSeconds(100L);
            assertThat(rateLimiter.overLimit(key)).isFalse();
        });

        IMap<Object, Object> map = hz.getMap(key);
        while (map.size() != 0) {
            Thread.sleep(10);
        }
        assertThat(map.size()).isZero();
    }

}
