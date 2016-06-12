package es.moki.ratelimitj;


import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class RedisScriptLoaderTest {

    private static RedisAsyncCommands<String, String> async;

    @BeforeClass
    public static void up() {
        async = RedisClient.create("redis://localhost").connect().async();
    }

    @AfterClass
    public static void down() {
        async.close();
    }

    @Test
    public void shouldLoadScript() throws Exception {
        RedisScriptLoader scriptLoader = new RedisScriptLoader(async, ClassLoader.getSystemResource("sliding-window-ratelimit.lua").toURI());

        assertThat(scriptLoader.scriptSha()).isNotEmpty();
    }
}