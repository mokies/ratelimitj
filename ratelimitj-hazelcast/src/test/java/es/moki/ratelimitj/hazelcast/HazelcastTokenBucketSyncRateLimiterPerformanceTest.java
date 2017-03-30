package es.moki.ratelimitj.hazelcast;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import es.moki.ratelimitj.core.api.LimitRule;
import es.moki.ratelimitj.core.api.RateLimiter;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.internal.test.AbstractSyncRateLimiterPerformanceTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.Set;


public class HazelcastTokenBucketSyncRateLimiterPerformanceTest extends AbstractSyncRateLimiterPerformanceTest {

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
        return new HazelcastTokenBucketRateLimiter(hz, rules, timeSupplier);
    }
}
