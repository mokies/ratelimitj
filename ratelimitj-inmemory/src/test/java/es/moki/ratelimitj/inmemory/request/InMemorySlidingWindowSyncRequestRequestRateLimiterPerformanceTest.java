package es.moki.ratelimitj.inmemory.request;

import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.test.limiter.request.AbstractSyncRequestRateLimiterPerformanceTest;

import java.util.Set;


public class InMemorySlidingWindowSyncRequestRequestRateLimiterPerformanceTest extends AbstractSyncRequestRateLimiterPerformanceTest {

    @Override
    protected RequestRateLimiter getRateLimiter(Set<RequestLimitRule> rules, TimeSupplier timeSupplier) {
        return new InMemorySlidingWindowRequestRateLimiter(rules, timeSupplier);
    }
}
