package es.moki.ratelimitj.redis;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import es.moki.ratelimitj.core.api.LimitRule;
import es.moki.ratelimitj.core.api.ReactiveRateLimiter;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.internal.test.AbstractReactiveRateLimiterTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.Set;


public class RedisSlidingWindowReactiveRateLimiterTest extends AbstractReactiveRateLimiterTest {

    private static RedisClient client;
    private static StatefulRedisConnection<String, String> connect;

    @BeforeAll
    public static void before() {
        client = RedisClient.create("redis://localhost");
        connect = client.connect();
    }

    @AfterAll
    public static void after() {
        connect.close();
        client.shutdown();
    }

    @AfterAll
    public void tearDown() {
        try (StatefulRedisConnection<String, String> connection = client.connect()) {
            connection.sync().flushdb();
        }
    }

    @Override
    protected ReactiveRateLimiter getRateLimiter(Set<LimitRule> rules, TimeSupplier timeSupplier) {
        return new RedisSlidingWindowRateLimiter(connect, rules, timeSupplier);
    }
}
