package es.moki.ratelimitj.inmemory;

import es.moki.ratelimitj.core.api.LimitRule;
import es.moki.ratelimitj.core.api.RateLimiter;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.inmemory.InMemorySlidingWindowRateLimiter;
import es.moki.ratelimitj.internal.test.AbstractSyncRateLimiterPerformanceTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.Set;


public class InMemorySlidingWindowSyncRateLimiterPerformanceTest extends AbstractSyncRateLimiterPerformanceTest {


    @BeforeAll
    public static void before() {

    }

    @AfterAll
    public static void after() {

    }

    @Override
    protected RateLimiter getRateLimiter(Set<LimitRule> rules, TimeSupplier timeSupplier) {
        return new InMemorySlidingWindowRateLimiter(rules, timeSupplier);
    }
}
