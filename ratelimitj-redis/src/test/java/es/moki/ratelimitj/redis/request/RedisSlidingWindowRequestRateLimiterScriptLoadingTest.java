package es.moki.ratelimitj.redis.request;

import com.google.common.collect.ImmutableSet;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.junit.jupiter.api.*;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class RedisSlidingWindowRequestRateLimiterScriptLoadingTest {


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

    @Test
    void shouldRetryWhenScriptIfFlushed() {

        ImmutableSet<RequestLimitRule> rules = ImmutableSet.of(RequestLimitRule.of(10, TimeUnit.SECONDS, 5));
        RedisSlidingWindowRequestRateLimiter requestRateLimiter = new RedisSlidingWindowRequestRateLimiter(connect, rules);

        assertThat(requestRateLimiter.overLimitWhenIncremented("ip:127.0.1.1")).isFalse();

        connect.sync().scriptFlush();

        requestRateLimiter.overLimitWhenIncremented("ip:127.0.1.1");

    }
}