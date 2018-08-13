package es.moki.ratelimitj.redis.request;

import es.moki.ratelimitj.core.limiter.request.*;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public class RedisClusterRateLimiterFactory extends AbstractRequestRateLimiterFactory<RedisSlidingWindowRequestRateLimiter> {

    private final RedisClusterClient client;
    private StatefulRedisClusterConnection<String, String> connection;

    public RedisClusterRateLimiterFactory(RedisClusterClient client) {
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

    protected RedisSlidingWindowRequestRateLimiter create(Set<RequestLimitRule> rules) {
        getConnection().reactive();
        return new RedisSlidingWindowRequestRateLimiter(getConnection().reactive(), getConnection().reactive(), rules);
    }

    @Override
    public void close() {
        client.shutdownAsync();
    }

    private StatefulRedisClusterConnection<String, String> getConnection() {
        // going to ignore race conditions at the cost of having multiple connections
        if (connection == null) {
            connection = client.connect();
        }
        return connection;
    }
}