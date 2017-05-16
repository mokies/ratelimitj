package es.moki.ratelimitj.core.limiter.request;


import java.io.Closeable;
import java.util.Set;

public interface RequestRateLimiterFactory extends Closeable {

    RequestRateLimiter getInstance(Set<RequestLimitRule> rules);

    AsyncRequestRateLimiter getInstanceAsync(Set<RequestLimitRule> rules);

    ReactiveRequestRateLimiter getInstanceReactive(Set<RequestLimitRule> rules);
}
