package es.moki.ratelimitj.redis.time;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import org.assertj.core.data.Offset;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class RedisTimeSupplierTest {

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

    @Test
    public void shouldGetSystemCurrentTime() {
        Long time = new RedisTimeSupplier(connect).get();
        assertThat(time).isCloseTo(System.currentTimeMillis() / 1000L, Offset.offset(50000L));
    }

    @Test
    public void shouldGetAsyncSystemCurrentTime() throws Exception {
        Long time = new RedisTimeSupplier(connect).getAsync().toCompletableFuture().get();
        assertThat(time).isCloseTo(System.currentTimeMillis() / 1000L, Offset.offset(50000L));
    }
}
