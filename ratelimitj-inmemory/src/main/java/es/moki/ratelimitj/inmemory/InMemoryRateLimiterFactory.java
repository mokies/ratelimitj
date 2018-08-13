package es.moki.ratelimitj.inmemory;


import es.moki.ratelimitj.core.limiter.request.AbstractRequestRateLimiterFactory;
import es.moki.ratelimitj.core.limiter.request.ReactiveRequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.inmemory.request.InMemorySlidingWindowRequestRateLimiter;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public class InMemoryRateLimiterFactory extends AbstractRequestRateLimiterFactory<InMemorySlidingWindowRequestRateLimiter> {

    @Override
    public RequestRateLimiter getInstance(Set<RequestLimitRule> rules) {
        requireNonNull(rules);
        return lookupInstance(rules);
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
    public void close() {

    }
}
