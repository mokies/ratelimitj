package es.moki.ratelimitj.redis.request;

import java.util.Set;

import es.moki.ratelimitj.core.limiter.request.AbstractRequestRateLimiterFactory;
import es.moki.ratelimitj.core.limiter.request.ReactiveRequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import org.redisson.api.RedissonReactiveClient;

import static java.util.Objects.requireNonNull;

public class RedisClusterRateLimiterFactory extends AbstractRequestRateLimiterFactory<RedisSlidingWindowRequestRateLimiter> {

    private final RedissonReactiveClient client;

    public RedisClusterRateLimiterFactory(RedissonReactiveClient client) {
        this.client = requireNonNull(client);
    }

    @Override
    public RequestRateLimiter getInstance(Set<RequestLimitRule> rules) {
        return lookupInstance(rules);
    }

    @Override
    public ReactiveRequestRateLimiter getInstanceReactive(Set<RequestLimitRule> rules) {
        return lookupInstance(rules);
    }

    @Override
    protected RedisSlidingWindowRequestRateLimiter create(Set<RequestLimitRule> rules) {
        return new RedisSlidingWindowRequestRateLimiter(client.getScript(), client.getKeys(), rules);
    }

    @Override
    public void close() {
        client.shutdown();
    }
}