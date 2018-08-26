package es.moki.ratelimitj.redis.request;


import es.moki.ratelimitj.redis.extensions.RedisStandaloneConnectionSetupExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static io.lettuce.core.ScriptOutputType.VALUE;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RedisScriptLoaderTest {

    @RegisterExtension
    static RedisStandaloneConnectionSetupExtension extension = new RedisStandaloneConnectionSetupExtension();

    private void scriptFlush() {
        extension.getScriptingReactiveCommands().scriptFlush().block();
    }

    @Test
    @DisplayName("should load rate limit lua script into Redis")
    void shouldLoadScript() {
        RedisScriptLoader scriptLoader = new RedisScriptLoader(extension.getScriptingReactiveCommands(), "hello-world.lua");
        scriptFlush();

        String sha = scriptLoader.storedScript().block(Duration.ofSeconds(5)).getSha();
        assertThat(sha).isNotEmpty();
        assertThat(extension.getScriptingReactiveCommands().scriptExists(sha).blockFirst()).isTrue();
    }

    @Test
    @DisplayName("should cache loaded sha")
    void shouldCache() {
        RedisScriptLoader scriptLoader = new RedisScriptLoader(extension.getScriptingReactiveCommands(), "hello-world.lua");

        assertThat(scriptLoader.storedScript().block(Duration.ofSeconds(5)).getSha()).isNotEmpty();

        scriptFlush();

        assertThat(scriptLoader.storedScript().block(Duration.ofSeconds(5)).getSha()).isNotEmpty();
    }

    @Test
    @DisplayName("should eagerly load rate limit lua script into Redis")
    void shouldEagerlyLoadScript() {
        RedisScriptLoader scriptLoader = new RedisScriptLoader(extension.getScriptingReactiveCommands(), "hello-world.lua", true);
        String sha = scriptLoader.storedScript().block(Duration.ofSeconds(5)).getSha();
        assertThat(sha).isNotEmpty();
        scriptFlush();

        new RedisScriptLoader(extension.getScriptingReactiveCommands(), "hello-world.lua", true);

        assertThat(extension.getScriptingReactiveCommands().scriptExists(sha).blockFirst()).isTrue();
    }

    @Test
    @DisplayName("should fail if script not found")
    void shouldFailedIfScriptNotFound() {

        Throwable exception = assertThrows(IllegalArgumentException.class,
                () -> new RedisScriptLoader(extension.getScriptingReactiveCommands(), "not-found-script.lua", true));
        assertThat(exception.getMessage()).contains("not found");
    }

    @Test
    @DisplayName("should fail if script not found")
    void shouldExecuteScript() {

        RedisScriptLoader scriptLoader = new RedisScriptLoader(extension.getScriptingReactiveCommands(), "hello-world.lua", true);
        String sha = scriptLoader.storedScript().block(Duration.ofSeconds(5)).getSha();

        Object result = extension.getScriptingReactiveCommands().evalsha(sha, VALUE).blockFirst();
        assertThat((String) result).isEqualTo("hello world");
    }

    @Test
    @DisplayName("should dispose stored script if scripted flushed from redis")
    void shouldReloadScriptIfFlushed() {

        RedisScriptLoader scriptLoader = new RedisScriptLoader(extension.getScriptingReactiveCommands(), "hello-world.lua", true);
        RedisScriptLoader.StoredScript storedScript = scriptLoader.storedScript().block(Duration.of(2, ChronoUnit.SECONDS));
        assertThat((String) extension.getScriptingReactiveCommands().evalsha(storedScript.getSha(), VALUE).blockFirst()).isEqualTo("hello world");

        scriptFlush();
        storedScript.dispose();

        storedScript = scriptLoader.storedScript().block(Duration.of(2, ChronoUnit.SECONDS));
        assertThat((String) extension.getScriptingReactiveCommands().evalsha(storedScript.getSha(), VALUE).blockFirst()).isEqualTo("hello world");
    }
}