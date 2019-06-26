package es.moki.ratelimitj.redis.request;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.redisson.api.RScriptReactive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.util.Objects.requireNonNull;

public class RedisScriptLoader {

    private static final Logger LOG = LoggerFactory.getLogger(RedisScriptLoader.class);

    private final RScriptReactive redisScriptingCommands;
    private final String scriptUri;
    private final AtomicReference<Flux<String>> storedScript;

    public RedisScriptLoader(RScriptReactive redisScriptingCommands, String scriptUri) {
        this(redisScriptingCommands, scriptUri, false);
    }

    public RedisScriptLoader(RScriptReactive redisScriptingCommands, String scriptUri, boolean eagerLoad) {
        requireNonNull(redisScriptingCommands);
        this.redisScriptingCommands = redisScriptingCommands;
        this.scriptUri = requireNonNull(scriptUri);

        this.storedScript = new AtomicReference<>(loadScript());

        if (eagerLoad) {
            this.storedScript.get().doOnComplete(() -> LOG.info("Redis Script eager load complete")).blockFirst(Duration.ofSeconds(10));
        }
    }

    Mono<StoredScript> storedScript() {
        return Mono.defer(() -> {
            Flux<String> source = this.storedScript.get();
            return source.next().map(sha -> new StoredScript(sha, source));
        });
    }

    private Flux<String> loadScript() {
        return Flux.defer(() -> {
            String script;
            try {
                script = readScriptFile();
            } catch (IOException e) {
                return Flux.error(new RuntimeException("Unable to load Redis LUA script file", e));
            }

            return redisScriptingCommands.scriptLoad(script);
        }).replay(1).autoConnect(1);
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

    class StoredScript {
        private final String sha;

        private final Flux<String> expected;

        StoredScript(String sha, Flux<String> expected) {
            this.sha = sha;
            this.expected = expected;
        }

        public String getSha() {
            return sha;
        }

        public void dispose() {
            storedScript.weakCompareAndSet(
                    expected,
                    loadScript()
            );
        }
    }

}
