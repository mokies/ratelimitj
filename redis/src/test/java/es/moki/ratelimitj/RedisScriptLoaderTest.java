package es.moki.ratelimitj;


import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class RedisScriptLoaderTest {

    private static RedisAsyncCommands<String, String> async;

    private static RedisClient client;

    @BeforeClass
    public static void up() {
        client = RedisClient.create("redis://localhost");
        async = client.connect().async();
    }

    @AfterClass
    public static void down() {
        async.close();
        client.shutdown();
    }

    @Test
    public void shouldLoadScript() throws Exception {
        RedisScriptLoader scriptLoader = new RedisScriptLoader(async, ClassLoader.getSystemResource("sliding-window-ratelimit.lua").toURI());

        assertThat(scriptLoader.scriptSha()).isNotEmpty();
    }
}