package es.moki.ratelimitj.hazelcast;

import com.google.common.collect.ImmutableSet;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import es.moki.ratelimitj.api.LimitRule;
import es.moki.ratelimitj.core.time.time.TimeBanditSupplier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class HazelcastSlidingWindowRateLimiterTest {

    private static final Logger LOG = LoggerFactory.getLogger(HazelcastSlidingWindowRateLimiterTest.class);

    private HazelcastInstance hz;

    private TimeBanditSupplier timeBandit = new TimeBanditSupplier();

    @Before
    public void setUp() {
        hz = Hazelcast.newHazelcastInstance();
    }

    @After
    public void after() {
        hz.shutdown();
    }

    @Test
    public void shouldLimitSingleWindowSync() throws Exception {

        ImmutableSet<LimitRule> rules = ImmutableSet.of(LimitRule.of(10, TimeUnit.SECONDS, 5));
        HazelcastSlidingWindowRateLimiter rateLimiter = new HazelcastSlidingWindowRateLimiter(hz, rules, timeBandit);

        IntStream.rangeClosed(1, 5).forEach(value -> {
            timeBandit.addUnixTimeMilliSeconds(1000L);
            assertThat(rateLimiter.overLimit("ip:127.0.0.5")).isFalse();
        });

        assertThat(rateLimiter.overLimit("ip:127.0.0.5")).isTrue();
    }

    @Test
    public void shouldLimitDualWindowSync() throws Exception {

        ImmutableSet<LimitRule> rules = ImmutableSet.of(LimitRule.of(2, TimeUnit.SECONDS, 5), LimitRule.of(10, TimeUnit.SECONDS, 20));
        HazelcastSlidingWindowRateLimiter rateLimiter = new HazelcastSlidingWindowRateLimiter(hz, rules, timeBandit);

        IntStream.rangeClosed(1, 5).forEach(value -> {
                    timeBandit.addUnixTimeMilliSeconds(200L);
                    assertThat(rateLimiter.overLimit("ip:127.0.0.10")).isFalse();

        });

        assertThat(rateLimiter.overLimit("ip:127.0.0.10")).isTrue();
        timeBandit.addUnixTimeMilliSeconds(1000L);
        assertThat(rateLimiter.overLimit("ip:127.0.0.10")).isFalse();
    }

}