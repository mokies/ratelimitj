package es.moki.ratelimitj.redis.request;

import es.moki.ratelimitj.core.limiter.request.ReactiveRequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.test.limiter.request.AbstractReactiveRequestRateLimiterTest;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import java.util.Set;


public class RedisSlidingWindowReactiveRequestRateLimiterTest extends AbstractReactiveRequestRateLimiterTest {

    private static RedisClient client;
    private static StatefulRedisConnection<String, String> connect;

    @BeforeAll
    public static void beforeAll() {
        client = RedisClient.create("redis://localhost");
        connect = client.connect();
    }

    @AfterAll
    public static void afterAll() {
        client.shutdownAsync();
    }

    @AfterEach
    public void afterEach() {
        try (StatefulRedisConnection<String, String> connection = client.connect()) {
            connection.sync().flushdb();
        }
    }

    @Override
    protected ReactiveRequestRateLimiter getRateLimiter(Set<RequestLimitRule> rules, TimeSupplier timeSupplier) {
        return new RedisSlidingWindowRequestRateLimiter(connect, rules, timeSupplier);
    }
}
