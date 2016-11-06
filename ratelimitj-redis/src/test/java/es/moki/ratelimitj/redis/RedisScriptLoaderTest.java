package es.moki.ratelimitj.redis;


import com.lambdaworks.redis.RedisClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class RedisScriptLoaderTest {

    private static RedisClient client;

    @BeforeAll
    public static void beforeAll() {
        client = RedisClient.create("redis://localhost");
    }

    @AfterAll
    public static void afterAll() {
        client.shutdown();
    }

    @Test
    public void shouldLoadScript() throws Exception {
        RedisScriptLoader scriptLoader = new RedisScriptLoader(client.connect(), "sliding-window-ratelimit.lua");

        assertThat(scriptLoader.scriptSha()).isNotEmpty();
    }
}