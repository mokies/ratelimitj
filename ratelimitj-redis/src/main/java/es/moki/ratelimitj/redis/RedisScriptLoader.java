package es.moki.ratelimitj.redis;


import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;

public class RedisScriptLoader {

    private final RedisAsyncCommands<String, String> async;
    private final String scriptUri;
    private final AtomicReference<String> scriptSha = new AtomicReference<>();

    public RedisScriptLoader(RedisAsyncCommands<String, String> async, String scriptUri) {
        this(async, scriptUri, true);
    }

    // TODO async seems unnecessary for this class
    public RedisScriptLoader(RedisAsyncCommands<String, String> async, String scriptUri, boolean eagerLoad) {
        this.async = requireNonNull(async);
        this.scriptUri = requireNonNull(scriptUri);
        if (eagerLoad) {
            loadScript();
        }
    }

    public String scriptSha() {
        String sha = scriptSha.get();

        if (sha == null) {
            loadScript();
            sha = scriptSha.get();
        }
        return sha;
    }

    private void loadScript() {
        String script;
        try {
            script = readScriptFile();
        } catch (IOException e) {
            throw new RuntimeException("Unable to load Redis LUA script file", e);
        }

        scriptSha.set(async.getStatefulConnection().sync().scriptLoad(script));
    }

    private String readScriptFile() throws IOException {
        // TODO remove guava depedency - java file loading is a mess!
        URL url = Resources.getResource(scriptUri);
        return Resources.toString(url, Charsets.UTF_8);
    }

}
