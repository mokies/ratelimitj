package es.moki.ratelimitj.redis;


import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisCommands;

import java.io.IOException;
import java.net.URL;

import static java.util.Objects.requireNonNull;

public class RedisScriptLoader {

    private final RedisCommands<String, String> async;
    private final String scriptUri;

    private volatile String shaInstance;

    public RedisScriptLoader(RedisAsyncCommands<String, String> async, String scriptUri) {
        this(async, scriptUri, true);
    }

    // TODO async seems unnecessary for this class
    public RedisScriptLoader(RedisAsyncCommands<String, String> async, String scriptUri, boolean eagerLoad) {
        requireNonNull(async);
        this.async = async.getStatefulConnection().sync();
        this.scriptUri = requireNonNull(scriptUri);
        if (eagerLoad) {
            scriptSha();
        }
    }

    String scriptSha() {
        // safe local double-checked locking - http://shipilev.net/blog/2014/safe-public-construction/
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

        return async.getStatefulConnection().sync().scriptLoad(script);
    }

    private String readScriptFile() throws IOException {
        // TODO remove guava depedency - java file loading is a mess!
        URL url = Resources.getResource(scriptUri);
        return Resources.toString(url, Charsets.UTF_8);
    }

}
