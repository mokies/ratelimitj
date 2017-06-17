package es.moki.ratelimitj.core.limiter.request;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractRequestRateLimiterFactory<T> implements RequestRateLimiterFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractRequestRateLimiterFactory.class.getClass());

    private final ConcurrentMap<Set<RequestLimitRule>, T> rateLimiterInstances = new ConcurrentHashMap<>();

    protected abstract T create(Set<RequestLimitRule> rules);

    protected T lookupInstance(Set<RequestLimitRule> rules) {
        T rateLimiter = rateLimiterInstances.get(rules);
            if (rateLimiter == null) {
                LOG.info("creating new RequestRateLimiter");
                rateLimiterInstances.putIfAbsent(rules, create(rules));
                // small race condition window, so lookup again
                rateLimiter = rateLimiterInstances.get(rules);
            }
        return rateLimiter;
    }


}
