package es.moki.ratelimitj.redis.request;

import es.moki.ratelimitj.core.limiter.request.AbstractRequestRateLimiterFactory;
import es.moki.ratelimitj.core.limiter.request.AsyncRequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.ReactiveRequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;

import java.io.IOException;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class RedisRateLimiterFactory extends AbstractRequestRateLimiterFactory<RedisSlidingWindowRequestRateLimiter> {

    private final RedisClient client;
    private StatefulRedisConnection<String, String> connection;

    public RedisRateLimiterFactory(RedisClient client) {
        this.client = requireNonNull(client);
    }

    @Override
    public RequestRateLimiter getInstance(Set<RequestLimitRule> rules) {
        return lookupInstance(rules);
    }

    @Override
    public AsyncRequestRateLimiter getInstanceAsync(Set<RequestLimitRule> rules) {
        return lookupInstance(rules);
    }

    @Override
    public ReactiveRequestRateLimiter getInstanceReactive(Set<RequestLimitRule> rules) {
        return lookupInstance(rules);
    }

    protected RedisSlidingWindowRequestRateLimiter create(Set<RequestLimitRule> rules) {
        return new RedisSlidingWindowRequestRateLimiter(getConnection(), rules);
    }

    @Override
    public void close() throws IOException {
        client.shutdownAsync();
    }

    private StatefulRedisConnection<String, String> getConnection() {
        // going to ignore race conditions at the cost of having multiple connections
        if (connection == null) {
            connection = client.connect();
        }
        return connection;
    }
}