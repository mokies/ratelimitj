package es.moki.ratelimitj.core.limiter.request;


import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractRequestRateLimiterFactory<T> implements RequestRateLimiterFactory {

    private final ConcurrentMap<Set<RequestLimitRule>, T> rateLimiterInstances = new ConcurrentHashMap<>();

    protected abstract T create(Set<RequestLimitRule> rules);

    protected T lookupInstance(Set<RequestLimitRule> rules) {
        return rateLimiterInstances.computeIfAbsent(rules, this::create);
    }

}
