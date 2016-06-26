package es.moki.ratelimitj.hazelcast;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import es.moki.ratelimitj.core.api.LimitRule;
import es.moki.ratelimitj.core.api.RateLimiter;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.internal.test.AbstractSyncRateLimiterTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.Set;


public class HazelcastSlidingWindowSyncRateLimiterTest extends AbstractSyncRateLimiterTest {

    private static HazelcastInstance hz;

    @BeforeClass
    public static void before() {
        hz = Hazelcast.newHazelcastInstance();
    }

    @AfterClass
    public static void after() {
        hz.shutdown();
    }

    @Override
    protected RateLimiter getRateLimiter(Set<LimitRule> rules, TimeSupplier timeSupplier) {
        return new HazelcastSlidingWindowRateLimiter(hz, rules, timeSupplier);
    }
}
