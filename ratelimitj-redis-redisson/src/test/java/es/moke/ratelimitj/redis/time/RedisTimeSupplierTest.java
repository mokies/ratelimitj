package es.moke.ratelimitj.redis.time;

import es.moki.ratelimitj.redis.time.RedisTimeSupplier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.config.Config;

import static org.assertj.core.api.Assertions.assertThat;

class RedisTimeSupplierTest
{

    private static RedissonReactiveClient client;

    @BeforeAll
    static void beforeAll() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://localhost:7006");

        client = Redisson.createReactive(config);
    }

    @AfterAll
    @SuppressWarnings("FutureReturnValueIgnored")
    static void afterAll() {
        client.shutdown();
    }

    @Test
    void shouldGetSystemCurrentTime() {
        Long time = new RedisTimeSupplier(client).get();
        assertThat(time).isNotNull().isNotNegative().isNotZero();
    }

    //TODO not implemented yet
    //@Test
    //void shouldGetAsyncSystemCurrentTime() throws Exception {
    //    Long time = new RedisTimeSupplier(connect).getAsync().toCompletableFuture().get();
    //    assertThat(time).isNotNull().isNotNegative().isNotZero();
    //}

    @Test
    void shouldGetReactiveSystemCurrentTime() {
        Long time = new RedisTimeSupplier(client).getReactive().block();
        assertThat(time).isNotNull().isNotNegative().isNotZero();
    }
}
