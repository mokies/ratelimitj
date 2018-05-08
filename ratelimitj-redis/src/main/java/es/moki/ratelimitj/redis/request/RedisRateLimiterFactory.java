package es.moki.ratelimitj.redis.request;

import es.moki.ratelimitj.core.limiter.request.AbstractRequestRateLimiterFactory;
import es.moki.ratelimitj.core.limiter.request.AsyncRequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.ReactiveRequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.cluster.RedisClusterClient;

import java.util.Set;

import static java.util.Objects.requireNonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisRateLimiterFactory extends AbstractRequestRateLimiterFactory<RedisSlidingWindowRequestRateLimiter> {
    private final Logger log = LoggerFactory.getLogger(RedisRateLimiterFactory.class);
    private final AbstractRedisClient client;
    private StatefulConnection<String, String> connection;

    public RedisRateLimiterFactory(AbstractRedisClient client) {
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
    public void close() {
        client.shutdownAsync();
    }

    private StatefulConnection<String, String> getConnection() {
        // going to ignore race conditions at the cost of having multiple connections
        if (connection == null) {
            if (RedisClusterClient.class.isInstance(client)) {
                connection = ((RedisClusterClient)client).connect();
            } else {
                connection = ((RedisClient)client).connect();
            }
        }
        return connection;
    }
}