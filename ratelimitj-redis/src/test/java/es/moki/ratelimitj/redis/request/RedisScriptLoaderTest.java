package es.moki.ratelimitj.redis.request;


import io.lettuce.core.RedisClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RedisScriptLoaderTest {

    private static RedisClient client;

    @BeforeAll
    static void beforeAll() {
        client = RedisClient.create("redis://localhost");
    }

    @AfterAll
    static void afterAll() {
        client.shutdownAsync();
    }

    @Test
    @DisplayName("should load rate limit lua script into Redis")
    void shouldLoadScript() {
        RedisScriptLoader scriptLoader = new RedisScriptLoader(client.connect(), "sliding-window-ratelimit.lua");

        assertThat(scriptLoader.scriptSha()).isNotEmpty();
    }

    @Test
    @DisplayName("should eagerly load rate limit lua script into Redis")
    void shouldEagerlyLoadScript() {
        RedisScriptLoader scriptLoader = new RedisScriptLoader(client.connect(), "sliding-window-ratelimit.lua", true);

        assertThat(scriptLoader.scriptSha()).isNotEmpty();
    }

    @Test
    @DisplayName("should fail if script not found")
    void shouldFailedIfScriptNotFound() {

        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            new RedisScriptLoader(client.connect(), "not-found-script.lua", true);
        });
        assertThat(exception.getMessage()).contains("not found");
    }
}