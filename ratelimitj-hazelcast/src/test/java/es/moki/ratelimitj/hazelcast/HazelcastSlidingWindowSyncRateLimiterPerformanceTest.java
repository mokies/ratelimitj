package es.moki.ratelimitj.hazelcast;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import es.moki.ratelimitj.api.LimitRule;
import es.moki.ratelimitj.api.RateLimiter;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.internal.test.AbstractSyncRateLimiterPerformanceTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.Set;


public class HazelcastSlidingWindowSyncRateLimiterPerformanceTest extends AbstractSyncRateLimiterPerformanceTest {

    private static HazelcastInstance hz;
//    private static HazelcastInstance hz2;

    @BeforeClass
    public static void before() {
        hz = Hazelcast.newHazelcastInstance();
//        hz2 = Hazelcast.newHazelcastInstance();
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
