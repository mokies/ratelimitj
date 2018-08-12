package es.moki.ratelimitj.redis.request;


import io.lettuce.core.api.StatefulRedisConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class RedisScriptLoader {

    private static final Logger LOG = LoggerFactory.getLogger(RedisScriptLoader.class);
    private final StatefulRedisConnection<String, String> connection;
    private final String scriptUri;
    private volatile String shaInstance;

    public RedisScriptLoader(StatefulRedisConnection<String, String> connection, String scriptUri) {
        this(connection, scriptUri, false);
    }

    public RedisScriptLoader(StatefulRedisConnection<String, String> connection, String scriptUri, boolean eagerLoad) {
        requireNonNull(connection);
        this.connection = connection;
        this.scriptUri = requireNonNull(scriptUri);
        if (eagerLoad) {
            scriptSha();
        }
    }

    String scriptSha() {
        // safe local double-checked locking
        // http://shipilev.net/blog/2014/safe-public-construction/
        String sha = shaInstance;
        if (sha == null) {
            synchronized (this) {
                sha = shaInstance;
                if (sha == null) {
                    sha = loadScript();
                    shaInstance = sha;
                }
            }
        }
        return sha;
    }

    String forceScriptShaReload() {
        LOG.info("force script reload");
        shaInstance = null;
        return scriptSha();
    }

    private String loadScript() {
        return connection.sync().scriptLoad(readScriptFile());
    }

    Mono<String> loadScriptReactive() {
        return connection.reactive().scriptLoad(readScriptFile());
    }

    private String readScriptFile() {
        URL url = RedisScriptLoader.class.getClassLoader().getResource(scriptUri);

        if (url == null) {
            throw new IllegalArgumentException("script '" + scriptUri + "' not found");
        }

        try {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to load Redis LUA script file", e);
        }
    }

}
