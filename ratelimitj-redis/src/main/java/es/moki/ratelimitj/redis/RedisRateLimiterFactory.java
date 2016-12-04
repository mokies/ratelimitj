package es.moki.ratelimitj.redis;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import es.moki.ratelimitj.core.api.AsyncRateLimiter;
import es.moki.ratelimitj.core.api.LimitRule;
import es.moki.ratelimitj.core.api.RateLimiter;
import es.moki.ratelimitj.core.api.RateLimiterFactory;
import es.moki.ratelimitj.core.api.ReactiveRateLimiter;

import java.io.IOException;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class RedisRateLimiterFactory implements RateLimiterFactory {

    private final RedisClient client;
    private final StatefulRedisConnection<String, String> connection;

    public RedisRateLimiterFactory(RedisClient client) {
        this.client = requireNonNull(client);
        this.connection = client.connect();
    }

    @Override
    public RateLimiter getInstance(Set<LimitRule> rules) {
        return new RedisSlidingWindowRateLimiter(connection, rules);
    }

    @Override
    public AsyncRateLimiter getInstanceAsync(Set<LimitRule> rules) {
        return new RedisSlidingWindowRateLimiter(connection, rules);
    }

    @Override
    public ReactiveRateLimiter getInstanceReactive(Set<LimitRule> rules) {
        return new RedisSlidingWindowRateLimiter(connection, rules);
    }

    @Override
    public void close() throws IOException {
        client.shutdown();
    }
}