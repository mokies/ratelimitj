package es.moki.ratelimitj.hazelcast;

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

    @Test @Ignore
    public void shouldLimitSingleWindow() throws Exception {

        ImmutableSet<LimitRule> rules = ImmutableSet.of(LimitRule.of(10, TimeUnit.SECONDS, 5));
        HazelcastSlidingWindowRateLimiter rateLimiter = new HazelcastSlidingWindowRateLimiter(hz, rules);

        IntStream.rangeClosed(1, 5).forEach(value -> {
            boolean result = rateLimiter.overLimit("ip:127.0.0.5");
            assertThat(result).isFalse();
        });

        assertThat(rateLimiter.overLimit("ip:127.0.0.5")).isTrue();

    }
}