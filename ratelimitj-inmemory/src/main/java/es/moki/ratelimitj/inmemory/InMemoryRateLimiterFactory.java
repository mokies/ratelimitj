package es.moki.ratelimitj.inmemory;


import es.moki.ratelimitj.core.limiter.request.AbstractRequestRateLimiterFactory;
import es.moki.ratelimitj.core.limiter.request.AsyncRequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.ReactiveRequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;

import java.io.IOException;
import java.util.Set;

public class InMemoryRateLimiterFactory extends AbstractRequestRateLimiterFactory<InMemorySlidingWindowRequestRateLimiter> {

    @Override
    public RequestRateLimiter getInstance(Set<RequestLimitRule> rules) {
        return lookupInstance(rules);
    }

    @Override
    public AsyncRequestRateLimiter getInstanceAsync(Set<RequestLimitRule> rules) {
        throw new RuntimeException("In memory async not yet implemented");
    }

    @Override
    public ReactiveRequestRateLimiter getInstanceReactive(Set<RequestLimitRule> rules) {
        throw new RuntimeException("In memory reactive not yet implemented");
    }

    @Override
    protected InMemorySlidingWindowRequestRateLimiter create(Set<RequestLimitRule> rules) {
        return new InMemorySlidingWindowRequestRateLimiter(rules);
    }

    @Override
    public void close() throws IOException {

    }
}
