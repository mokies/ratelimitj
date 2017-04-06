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
    private StatefulRedisConnection<String, String> connection;

    public RedisRateLimiterFactory(RedisClient client) {
        this.client = requireNonNull(client);
    }

    @Override
    public RateLimiter getInstance(Set<LimitRule> rules) {
        return new RedisTokenBucketRateLimiter(getConnection(), rules);
    }

    @Override
    public AsyncRateLimiter getInstanceAsync(Set<LimitRule> rules) {
        return new RedisTokenBucketRateLimiter(getConnection(), rules);
    }

    @Override
    public ReactiveRateLimiter getInstanceReactive(Set<LimitRule> rules) {
        return new RedisTokenBucketRateLimiter(getConnection(), rules);
    }

    @Override
    public void close() throws IOException {
        client.shutdown();
    }

    private StatefulRedisConnection<String, String> getConnection() {
        // going to ignore race conditions at the cost of having multiple connections
        if (connection == null) {
            connection = client.connect();
        }
        return connection;
    }
}