package es.moki.ratelimitj.hazelcast;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import es.moki.ratelimitj.api.LimitRule;
import es.moki.ratelimitj.core.time.time.TimeBanditSupplier;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@Deprecated
public class HazelcastSlidingWindowRateLimiterTest {

    private static final Logger LOG = LoggerFactory.getLogger(HazelcastSlidingWindowRateLimiterTest.class);

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

    @Test
    public void shouldLimitSingleWindowSync() throws Exception {

        ImmutableSet<LimitRule> rules =
                ImmutableSet.of(LimitRule.of(10, TimeUnit.SECONDS, 5));
        HazelcastSlidingWindowRateLimiter rateLimiter =
                new HazelcastSlidingWindowRateLimiter(hz, rules, timeBandit);

        IntStream.rangeClosed(1, 5).forEach(value -> {
            timeBandit.addUnixTimeMilliSeconds(1000L);
            assertThat(rateLimiter.overLimit("ip:127.0.0.5")).isFalse();
        });

        assertThat(rateLimiter.overLimit("ip:127.0.0.5")).isTrue();
    }

    @Test
    public void shouldLimitDualWindowSync() throws Exception {

        ImmutableSet<LimitRule> rules =
                ImmutableSet.of(LimitRule.of(2, TimeUnit.SECONDS, 5), LimitRule.of(10, TimeUnit.SECONDS, 20));
        HazelcastSlidingWindowRateLimiter rateLimiter =
                new HazelcastSlidingWindowRateLimiter(hz, rules, timeBandit);

        IntStream.rangeClosed(1, 5).forEach(value -> {
            timeBandit.addUnixTimeMilliSeconds(200L);
            assertThat(rateLimiter.overLimit("ip:127.0.0.10")).isFalse();

        });

        assertThat(rateLimiter.overLimit("ip:127.0.0.10")).isTrue();
        timeBandit.addUnixTimeMilliSeconds(1000L);
        assertThat(rateLimiter.overLimit("ip:127.0.0.10")).isFalse();
    }

    @Test
    public void shouldLimitDualWindowSyncTimed() throws Exception {

        Stopwatch watch = Stopwatch.createStarted();

        ImmutableSet<LimitRule> rules =
                ImmutableSet.of(LimitRule.of(2, TimeUnit.SECONDS, 100), LimitRule.of(10, TimeUnit.SECONDS, 100));
        HazelcastSlidingWindowRateLimiter rateLimiter =
                new HazelcastSlidingWindowRateLimiter(hz, rules, timeBandit);
        Random r = new Random();

        int total = 10_000;
        IntStream.rangeClosed(1, total).map(i -> r.nextInt(128)).forEach(value -> {
            timeBandit.addUnixTimeMilliSeconds(200L);
            rateLimiter.overLimit("ip:127.0.0." + value);
        });

        LOG.info("total time " + watch.stop()
                + " checks " + ((total / watch.elapsed(TimeUnit.MILLISECONDS))*1000) + "/sec");
    }

}