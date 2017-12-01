package es.moki.ratelimitj.redis.time;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RedisTimeSupplierTest {

    private static RedisClient client;
    private static StatefulRedisConnection<String, String> connect;

    @BeforeAll
    static void beforeAll() {
        client = RedisClient.create("redis://localhost");
        connect = client.connect();
    }

    @AfterAll
    static void afterAll() {
        client.shutdownAsync();
    }

    @Test
    void shouldGetSystemCurrentTime() {
        Long time = new RedisTimeSupplier(connect).get();
        assertThat(time).isNotNull().isNotNegative().isNotZero();
    }

    @Test
    void shouldGetAsyncSystemCurrentTime() throws Exception {
        Long time = new RedisTimeSupplier(connect).getAsync().toCompletableFuture().get();
        assertThat(time).isNotNull().isNotNegative().isNotZero();
    }

    @Test
    void shouldGetReactiveSystemCurrentTime() {
        Long time = new RedisTimeSupplier(connect).getReactive().block();
        assertThat(time).isNotNull().isNotNegative().isNotZero();
    }
}
