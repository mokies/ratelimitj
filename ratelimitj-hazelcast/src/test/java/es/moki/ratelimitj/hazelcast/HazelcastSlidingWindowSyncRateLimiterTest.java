package es.moki.ratelimitj.hazelcast;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import es.moki.ratelimitj.core.api.LimitRule;
import es.moki.ratelimitj.core.api.RateLimiter;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.internal.test.AbstractSyncRateLimiterTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Set;


public class HazelcastSlidingWindowSyncRateLimiterTest extends AbstractSyncRateLimiterTest {

    private static HazelcastInstance hz;

    @BeforeAll
    public static void before() {
        hz = Hazelcast.newHazelcastInstance();
    }

    @AfterAll
    public static void after() {
        hz.shutdown();
    }

    @Override
    protected RateLimiter getRateLimiter(Set<LimitRule> rules, TimeSupplier timeSupplier) {
        return new HazelcastSlidingWindowRateLimiter(hz, rules, timeSupplier);
    }

    @Override @Test @Disabled
    public void shouldResetLimit() {

    }

}
