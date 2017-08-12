package es.moki.ratelimitj.redis.request;


import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.lettuce.core.api.StatefulRedisConnection;

import java.io.IOException;
import java.net.URL;

import static java.util.Objects.requireNonNull;

public class RedisScriptLoader {

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

    private String loadScript() {
        String script;
        try {
            script = readScriptFile();
        } catch (IOException e) {
            throw new RuntimeException("Unable to load Redis LUA script file", e);
        }

        return connection.sync().scriptLoad(script);
    }

    private String readScriptFile() throws IOException {
        // TODO remove guava depedency - java file loading is a mess!
        URL url = Resources.getResource(scriptUri);
        return Resources.toString(url, Charsets.UTF_8);
    }

}
