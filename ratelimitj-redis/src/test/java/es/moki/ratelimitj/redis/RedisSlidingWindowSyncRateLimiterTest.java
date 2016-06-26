package es.moki.ratelimitj.redis;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import es.moki.ratelimitj.core.api.LimitRule;
import es.moki.ratelimitj.core.api.RateLimiter;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.internal.test.AbstractSyncRateLimiterTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.Set;


public class RedisSlidingWindowSyncRateLimiterTest extends AbstractSyncRateLimiterTest {

    private static RedisClient client;
    private static StatefulRedisConnection<String, String> connect;

    @BeforeClass
    public static void before() {
        client = RedisClient.create("redis://localhost");
        connect = client.connect();
    }

    @AfterClass
    public static void after() {
        connect.close();
        client.shutdown();
    }

    @After
    public void tearDown() {
        try (StatefulRedisConnection<String, String> connection = client.connect()) {
            connection.sync().flushdb();
        }
    }

    @Override
    protected RateLimiter getRateLimiter(Set<LimitRule> rules, TimeSupplier timeSupplier) {
        return new RedisSlidingWindowRateLimiter(connect, rules, timeSupplier);
    }
}
