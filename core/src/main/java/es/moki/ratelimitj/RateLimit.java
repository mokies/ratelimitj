package es.moki.ratelimitj;


import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import static com.lambdaworks.redis.ScriptOutputType.INTEGER;
import static java.util.Objects.requireNonNull;

public class RateLimit implements AutoCloseable {

    private final RedisClient client;
    private final RedisAsyncCommands<String, String> async;
    private final RedisScriptLoader scriptLoader;
    private final String rulesJson;

    public RateLimit(String redisHost, Set<Window> rules) {
        client = RedisClient.create(redisHost);
        async = client.connect().async();
        scriptLoader = new RedisScriptLoader(async, limitScript());
        this.rulesJson = toJsonString(requireNonNull(rules));
    }

    private URI limitScript() {
        try {
            return ClassLoader.getSystemResource("limit.lua").toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to load limit.lua",e);
        }
    }

    private String toJsonString(Set<Window> rules) {
        Iterator<Window> iterator = rules.iterator();
        return "[" + iterator.next().toJsonObject().toString() + "," + iterator.next().toJsonObject().toString() + "]";
    }

    public CompletionStage<Boolean> overLimitSliderWindow(String key) {
        return overLimitSliderWindow(key, 1);
    }

    public CompletionStage<Boolean> overLimitSliderWindow(String key, int weight) {

        String sha = scriptLoader.scriptSha();

        String rules = "[ {\"interval\": 1, \"limit\": 5}, {\"interval\": 3600, \"limit\": 1000, \"precision\": 100} ]";

        String epochSecond = Long.toString(Instant.now().getEpochSecond());

        RedisFuture<String> rateResult = async.evalsha(sha, INTEGER, new String[]{key}, rulesJson, epochSecond, Integer.toString(weight));

        return rateResult.thenApply("1"::equals);
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
