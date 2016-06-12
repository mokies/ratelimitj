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
public class RedisRateLimit implements AutoCloseable, AsyncRateLimiter, RateLimiter {

    private static final Logger LOG = LoggerFactory.getLogger(RedisRateLimit.class);

    private final RedisAsyncCommands<String, String> async;
    private final RedisScriptLoader scriptLoader;
    private final String rulesJson;
    private final boolean useRedisTime;

    // TODO Might require a configuration factory.

    public RedisRateLimit(RedisClient redisClient, Set<Window> rules) {
        this(redisClient, rules, false);
    }

    public RedisRateLimit(RedisClient redisClient, Set<Window> rules, boolean useRedisTime) {
        async = redisClient.connect().async();
        scriptLoader = new RedisScriptLoader(async, limitScript());
        rulesJson = toJsonArray(requireNonNull(rules));
        this.useRedisTime = useRedisTime;
    }

    private URI limitScript() {
        try {
            return ClassLoader.getSystemResource("sliding-window-ratelimit.lua").toURI();
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
        requireNonNull(key);

        // TODO load script completely async
        String sha = scriptLoader.scriptSha();

//        return currentUnixTimeSeconds()
//                .thenAccept(now -> {
//                    LOG.debug("time {}", now);
//                    async.evalsha(sha, VALUE, new String[]{key}, rulesJson, now, Integer.toString(weight));})
//                .thenApply("1"::equals);


        // TODO complete async use redis time
        return  async.evalsha(sha, VALUE, new String[]{key}, rulesJson, Long.toString(Instant.now().getEpochSecond()), Integer.toString(weight))
                .thenApply("1"::equals);


        // TODO handle scenario where script is not loaded, flush scripts and test scenario
    }

    @Override
    public boolean overLimit(String key) {
        return overLimit(key, 1);
    }

    @Override
    public boolean overLimit(String key, int weight) {
        try {
            return overLimitAsync(key, weight).toCompletableFuture().get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CompletionStage<String> currentUnixTimeSeconds() {
        if (useRedisTime) {
            return async.time().thenApply(strings -> strings.get(0));
        }
        return CompletableFuture.completedFuture(Long.toString(System.currentTimeMillis() / 1000L));
    }

    @Override
    public void close() throws Exception {
        async.close();
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
