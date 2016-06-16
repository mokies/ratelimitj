package es.moki.ratelimitj.redis;


import com.lambdaworks.redis.RedisClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class RedisScriptLoaderTest {

    private static RedisClient client;

    @BeforeClass
    public static void up() {
        client = RedisClient.create("redis://localhost");
    }

    @AfterClass
    public static void down() {
        client.shutdown();
    }

    @Test
    public void shouldLoadScript() throws Exception {
        RedisScriptLoader scriptLoader = new RedisScriptLoader(client.connect(), "sliding-window-ratelimit.lua");

        assertThat(scriptLoader.scriptSha()).isNotEmpty();
    }
}