package es.moki.ratelimitj;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static com.lambdaworks.redis.ScriptOutputType.VALUE;
import static java.util.Objects.requireNonNull;

@ThreadSafe
public class RedisRateLimit implements AutoCloseable { //,RateLimit {

    private static final Logger LOG = LoggerFactory.getLogger(RedisRateLimit.class);

    private final RedisClient client;
    private final RedisAsyncCommands<String, String> async;
    private final RedisScriptLoader scriptLoader;
    private final String rulesJson;
    private final boolean useRedisTime;

    // TODO allow client to be passed in so that client can be shared. Might require a configuration factory.

    public RedisRateLimit(String redisHost, Set<Window> rules) {
        this(redisHost, rules, false);
    }

    public RedisRateLimit(String redisHost, Set<Window> rules, boolean useRedisTime) {
        client = RedisClient.create(redisHost);
        async = client.connect().async();
        scriptLoader = new RedisScriptLoader(async, limitScript());
        this.rulesJson = toJsonArray(requireNonNull(rules));
        this.useRedisTime = useRedisTime;
    }

    private URI limitScript() {
        try {
            return ClassLoader.getSystemResource("limit.lua").toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to load limit.lua", e);
        }
    }

    private String toJsonArray(Set<Window> rules) {
        JsonArray jsonArray = Json.array().asArray();
        rules.forEach(window -> jsonArray.add(window.toJsonArray()));
        String rulesJson = jsonArray.toString();
        LOG.debug("Rules {}", rulesJson);
        return rulesJson;
    }

    public CompletionStage<Boolean> overLimitAsync(String key) {
        return overLimitAsync(key, 1);
    }

    public CompletionStage<Boolean> overLimitAsync(String key, int weight) {

        // TODO load script completely async
        String sha = scriptLoader.scriptSha();

//        return nowEpochSeconds()
//                .thenAccept(now -> {
//                    LOG.debug("time {}", now);
//                    async.evalsha(sha, VALUE, new String[]{key}, rulesJson, now, Integer.toString(weight));})
//                .thenApply("1"::equals);


        // TODO complete async use redis time
        return  async.evalsha(sha, VALUE, new String[]{key}, rulesJson, Long.toString(Instant.now().getEpochSecond()), Integer.toString(weight))
                .thenApply("1"::equals);


        // TODO handle scenario where script is not loaded, flush scripts and test scenario
    }

    public boolean overLimit(String key) {
        try {
            return overLimitAsync(key).toCompletableFuture().get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private CompletionStage<String> nowEpochSeconds() {
        if (useRedisTime) {
            return async.time().thenApply(strings -> strings.get(0));
        }
        return CompletableFuture.completedFuture(Long.toString(Instant.now().getEpochSecond()));
    }

    @Override
    public void close() throws Exception {
        async.close();
        client.shutdown();
    }


//
//    def over_limit_sliding_window(conn, weight=1, limits=[(1, 10), (60, 120), (3600, 240, 60)], redis_time=False):
//            if not hasattr(conn, 'over_limit_sliding_window_lua'):
//    conn.over_limit_sliding_window_lua = conn.register_script(over_limit_sliding_window_lua_)
//
//    now = conn.time()[0] if redis_time else time.time()
//            return conn.over_limit_sliding_window_lua(
//    keys=get_identifiers(), args=[json.dumps(limits), now, weight])
}
