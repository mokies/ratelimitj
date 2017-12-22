package es.moki.ratelimitj.redis.request;


import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.lettuce.core.ScriptOutputType.VALUE;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RedisScriptLoaderTest {

    private static RedisClient client;
    private static StatefulRedisConnection<String, String> connection;

    @BeforeAll
    static void beforeAll() {
        client = RedisClient.create("redis://localhost");
        connection = client.connect();
    }

    @AfterAll
    static void afterAll() {
        client.shutdownAsync();
    }

    @Test
    @DisplayName("should load rate limit lua script into Redis")
    void shouldLoadScript() {
        RedisScriptLoader scriptLoader = new RedisScriptLoader(connection, "hello-world.lua");

        assertThat(scriptLoader.scriptSha()).isNotEmpty();
    }

    @Test
    @DisplayName("should eagerly load rate limit lua script into Redis")
    void shouldEagerlyLoadScript() {
        RedisScriptLoader scriptLoader = new RedisScriptLoader(connection, "hello-world.lua", true);

        assertThat(scriptLoader.scriptSha()).isNotEmpty();
    }

    @Test
    @DisplayName("should fail if script not found")
    void shouldFailedIfScriptNotFound() {

        Throwable exception = assertThrows(IllegalArgumentException.class,
                () -> new RedisScriptLoader(connection, "not-found-script.lua", true));
        assertThat(exception.getMessage()).contains("not found");
    }

    @Test
    @DisplayName("should fail if script not found")
    void shouldExecuteScript() {

        RedisScriptLoader scriptLoader = new RedisScriptLoader(connection, "hello-world.lua", true);
        String sha = scriptLoader.scriptSha();

        Object result = connection.sync().evalsha(sha, VALUE);
        assertThat((String) result).isEqualTo("hello world");
    }
}