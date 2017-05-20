package es.moki.ratelimitj.redis;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import es.moki.ratelimitj.core.limiter.request.AsyncRequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.ReactiveRequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class RedisRateLimiterFactory implements RequestRateLimiterFactory {

    private static final Logger LOG = LoggerFactory.getLogger(RedisRateLimiterFactory.class);

    private final RedisClient client;
    private StatefulRedisConnection<String, String> connection;

    public RedisRateLimiterFactory(RedisClient client) {
        this.client = requireNonNull(client);
    }

    @Override
    public RequestRateLimiter getInstance(Set<RequestLimitRule> rules) {
        return create(rules);
    }

    @Override
    public AsyncRequestRateLimiter getInstanceAsync(Set<RequestLimitRule> rules) {
        return create(rules);
    }

    @Override
    public ReactiveRequestRateLimiter getInstanceReactive(Set<RequestLimitRule> rules) {
        return create(rules);
    }

    private RedisSlidingWindowRequestRateLimiter create(Set<RequestLimitRule> rules) {
        LOG.info("creating new RedisSlidingWindowRequestRateLimiter");
        return new RedisSlidingWindowRequestRateLimiter(getConnection(), rules);
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