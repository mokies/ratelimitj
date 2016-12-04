package es.moki.ratelimitj.core.api;


import java.io.Closeable;
import java.util.Set;

public interface RateLimiterFactory extends Closeable {

    RateLimiter getInstance(Set<LimitRule> rules);

    AsyncRateLimiter getInstanceAsync(Set<LimitRule> rules);

    ReactiveRateLimiter getInstanceReactive(Set<LimitRule> rules);
}
