package es.moki.ratelimitj.redis.request;


import io.lettuce.core.api.StatefulRedisConnection;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class RedisScriptLoader {

    private final StatefulRedisConnection<String, String> connection;
    private final String scriptUri;
    private final Flux<StoredScript> storedScript;

    private Disposable cacheDisposable;

    public RedisScriptLoader(StatefulRedisConnection<String, String> connection, String scriptUri) {
        this(connection, scriptUri, false);
    }

    public RedisScriptLoader(StatefulRedisConnection<String, String> connection, String scriptUri, boolean eagerLoad) {
        requireNonNull(connection);
        this.connection = connection;
        this.scriptUri = requireNonNull(scriptUri);

        this.storedScript = Flux
                .defer(this::loadScript)
                .replay(1)
                .autoConnect(1, disposable -> {
                    this.cacheDisposable = disposable;
                });

        if (eagerLoad) {
            this.storedScript.blockFirst(Duration.ofSeconds(10));
        }
    }

    Mono<StoredScript> storedScript() {
        return storedScript.next();
    }

    private Mono<StoredScript> loadScript() {
        String script;
        try {
            script = readScriptFile();
        } catch (IOException e) {
            return Mono.error(new RuntimeException("Unable to load Redis LUA script file", e));
        }

        return connection.reactive()
                .scriptLoad(script)
                .map(sha -> new StoredScript(sha, cacheDisposable));
    }

    private String readScriptFile() throws IOException {
        URL url = RedisScriptLoader.class.getClassLoader().getResource(scriptUri);

        if (url == null) {
            throw new IllegalArgumentException("script '" + scriptUri + "' not found");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    static class StoredScript implements Disposable {
        private String sha;

        private Disposable disposable;

        StoredScript(String sha, Disposable disposable) {
            this.sha = sha;
            this.disposable = disposable;
        }

        public String getSha() {
            return sha;
        }

        @Override
        public boolean isDisposed() {
            return disposable.isDisposed();
        }

        @Override
        public void dispose() {
            disposable.dispose();
        }
    }

}
