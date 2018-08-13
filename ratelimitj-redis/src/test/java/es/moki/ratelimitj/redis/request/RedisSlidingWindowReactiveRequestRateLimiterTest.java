package es.moki.ratelimitj.redis.request;

import com.google.common.collect.ImmutableSet;
import es.moki.ratelimitj.core.limiter.request.ReactiveRequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.time.SystemTimeSupplier;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.test.limiter.request.AbstractReactiveRequestRateLimiterTest;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class RedisSlidingWindowReactiveRequestRateLimiterTest extends AbstractReactiveRequestRateLimiterTest {

    private static RedisClient client;
    private static StatefulRedisConnection<String, String> connect;

    @BeforeAll
    static void beforeAll() {
        client = RedisClient.create("redis://localhost");
        connect = client.connect();
    }

    @AfterAll
    @SuppressWarnings("FutureReturnValueIgnored")
    static void afterAll() {
        client.shutdownAsync();
    }

    @AfterEach
    void afterEach() {
        try (StatefulRedisConnection<String, String> connection = client.connect()) {
            connection.sync().flushdb();
        }
    }

    @Override
    protected ReactiveRequestRateLimiter getRateLimiter(Set<RequestLimitRule> rules, TimeSupplier timeSupplier) {
        return new RedisSlidingWindowRequestRateLimiter(connect.reactive(), connect.reactive(), rules, timeSupplier);
    }

    @Test
    void shouldReloadMissingScript() {
        ImmutableSet<RequestLimitRule> rules = ImmutableSet.of(RequestLimitRule.of(60, TimeUnit.SECONDS, 1));
        ReactiveRequestRateLimiter rateLimiter = getRateLimiter(rules, new SystemTimeSupplier());

        rateLimiter.overLimitWhenIncrementedReactive(UUID.randomUUID().toString()).block(Duration.ofSeconds(5));

        connect.sync().scriptFlush();

        rateLimiter.overLimitWhenIncrementedReactive(UUID.randomUUID().toString()).block(Duration.ofSeconds(5));
    }
}
