package es.moki.ratelimitj.inmemory;


import es.moki.ratelimitj.core.limiter.request.AsyncRequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.ReactiveRequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

public class InMemoryRateLimiterFactory implements RequestRateLimiterFactory {

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryRateLimiterFactory.class);

    @Override
    public RequestRateLimiter getInstance(Set<RequestLimitRule> rules) {
        LOG.info("creating new InMemorySlidingWindowRequestRateLimiter");
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

    private RequestRateLimiter lookupInstance(Set<RequestLimitRule> rules) {
        return new InMemorySlidingWindowRequestRateLimiter(rules);
    }

    @Override
    public void close() throws IOException {

    }
}
