package es.moki.ratelimitj.hazelcast;


import com.google.common.collect.ImmutableSet;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import es.moki.ratelimitj.api.LimitRule;
import es.moki.ratelimitj.api.RateLimiter;
import es.moki.ratelimitj.core.time.time.TimeBanditSupplier;
import es.moki.ratelimitj.core.time.time.TimeSupplier;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class HazelcastRateLimiterInternalTest {

    private static HazelcastInstance hz;

    private TimeBanditSupplier timeBandit = new TimeBanditSupplier();

    @BeforeClass
    public static void before() {
        hz = Hazelcast.newHazelcastInstance();
    }

    @AfterClass
    public static void after() {
        hz.shutdown();
    }

    protected RateLimiter getRateLimiter(Set<LimitRule> rules, TimeSupplier timeSupplier) {
        return new HazelcastSlidingWindowRateLimiter(hz, rules, timeSupplier);
    }

    @Test
    public void shouldCleanUpExpiredKeys() throws Exception {
        ImmutableSet<LimitRule> rules = ImmutableSet.of(LimitRule.of(2, TimeUnit.SECONDS, 5));
        RateLimiter rateLimiter = getRateLimiter(rules, timeBandit);

        IntStream.rangeClosed(1, 5).forEach(value -> {
            timeBandit.addUnixTimeMilliSeconds(100L);
            assertThat(rateLimiter.overLimit("ip:127.0.0.5")).isFalse();
        });

        System.out.println(hz.getMap("ip:127.0.0.5").size());

        Thread.sleep(2500);

        System.out.println(hz.getMap("ip:127.0.0.5").size());
        assertThat(hz.getMap("ip:127.0.0.5").size()).isZero();


    }

}
