package es.moki.ratelimitj.hazelcast;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import es.moki.ratelimitj.core.LimitRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class HazelcastSlidingWindowRateLimiterTest {

    private HazelcastInstance hz;

    @Before
    public void setUp() {
        hz = Hazelcast.newHazelcastInstance();
    }

    @After
    public void after() {
        hz.shutdown();
    }

    @Test
    public void shouldLimitSingleWindow() throws Exception {

        ImmutableSet<LimitRule> rules = ImmutableSet.of(LimitRule.of(1000, TimeUnit.SECONDS, 5));
        HazelcastSlidingWindowRateLimiter rateLimiter = new HazelcastSlidingWindowRateLimiter(hz, rules);

        IntStream.rangeClosed(1, 5).forEach(value -> {
            boolean result = rateLimiter.overLimit("ip:127.0.0.5");
            assertThat(result).isFalse();
        });

        assertThat(rateLimiter.overLimit("ip:127.0.0.5")).isTrue();
    }

    @Test
    public void shouldLimitDualWindowAsync() throws Exception {

        ImmutableSet<LimitRule> rules = ImmutableSet.of(LimitRule.of(5, TimeUnit.SECONDS, 5), LimitRule.of(50, TimeUnit.SECONDS, 20));
        HazelcastSlidingWindowRateLimiter rateLimiter = new HazelcastSlidingWindowRateLimiter(hz, rules);

        for(int i=1; i<5; i++){
            assertThat(rateLimiter.overLimit("ip:127.0.0.10")).isFalse();
        }

        boolean lastResult = false;
        for(int i=1; i<20; i++){
            lastResult = rateLimiter.overLimit("ip:127.0.0.10");
        }
        assertThat(lastResult).isTrue();

        Thread.sleep(2000);
        assertThat(rateLimiter.overLimit("ip:127.0.0.10")).isFalse();
    }

    @Test @Ignore
    public void shouldPerformFastSingleWindow() throws Exception {

        ImmutableSet<LimitRule> rules = ImmutableSet.of(LimitRule.of(1, TimeUnit.MINUTES, 100));
        HazelcastSlidingWindowRateLimiter rateLimiter = new HazelcastSlidingWindowRateLimiter(hz, rules);

        Stopwatch started = Stopwatch.createStarted();
        for(int i=1; i< 10000; i++){
            rateLimiter.overLimit("ip:127.0.0.11");
        }

        started.stop();
        System.out.println(started);
    }
}