package es.moki.ratelimitj.inmemory;

import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.test.AbstractSyncRateLimiterPerformanceTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.Set;


public class InMemorySlidingWindowSyncRequestRateLimiterPerformanceTest extends AbstractSyncRateLimiterPerformanceTest {


    @BeforeAll
    public static void before() {

    }

    @AfterAll
    public static void after() {

    }

    @Override
    protected RequestRateLimiter getRateLimiter(Set<RequestLimitRule> rules, TimeSupplier timeSupplier) {
        return new InMemorySlidingWindowRequestRateLimiter(rules, timeSupplier);
    }
}
