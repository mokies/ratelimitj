package es.moki.ratelimitj;


import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;

public class RedisScriptLoader {

    private final RedisAsyncCommands<String, String> async;
    private final URI scriptUri;
    private final AtomicReference<String> scriptSha = new AtomicReference<>();

    public RedisScriptLoader(RedisAsyncCommands<String, String> async, URI scriptUri) {
        this(async, scriptUri, true);
    }

    public RedisScriptLoader(RedisAsyncCommands<String, String> async, URI scriptUri, boolean eagerLoad) {
        this.async = requireNonNull(async);
        this.scriptUri = requireNonNull(scriptUri);
        if (eagerLoad) {
            loadScript();
        }
    }

    public RedisFuture<String> loadScript() {
         String script;
         try {
             // TODO Load file asynchronously
             script = new String(Files.readAllBytes(Paths.get(scriptUri)));
         } catch (Exception e) {
             throw new RuntimeException(e);
         }

        return async.scriptLoad(script);
     }

     public String scriptSha()  {
         String sha = scriptSha.get();
         if (sha == null) {
             RedisFuture<String> shaFuture = loadScript();
             shaFuture.thenAccept(scriptSha::set);
             try {
                sha = shaFuture.get(10, TimeUnit.SECONDS);
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
         return sha;
     }
}
