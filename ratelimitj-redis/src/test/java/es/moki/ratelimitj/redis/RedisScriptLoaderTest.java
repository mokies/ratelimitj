package es.moki.ratelimitj.redis;


import com.lambdaworks.redis.RedisClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.expectThrows;

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
    @DisplayName("should load rate limit lua script into Redis")
    public void shouldLoadScript() {
        RedisScriptLoader scriptLoader = new RedisScriptLoader(client.connect(), "token-bucket-ratelimit.lua");

        assertThat(scriptLoader.scriptSha()).isNotEmpty();
    }

    @Test
    @DisplayName("should eagerly load rate limit lua script into Redis")
    public void shouldEagerlyLoadScript() {
        RedisScriptLoader scriptLoader = new RedisScriptLoader(client.connect(), "token-bucket-ratelimit.lua", true);

        assertThat(scriptLoader.scriptSha()).isNotEmpty();
    }

    @Test
    @DisplayName("should fail if script not found")
    public void shouldFailedIfScriptNotFound() {

        Throwable exception = expectThrows(IllegalArgumentException.class, () -> {
            new RedisScriptLoader(client.connect(), "not-found-script.lua", true);
        });
        assertThat(exception.getMessage()).contains("not found");
    }
}