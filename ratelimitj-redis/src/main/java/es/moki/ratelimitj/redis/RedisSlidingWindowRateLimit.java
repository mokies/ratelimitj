package es.moki.ratelimitj.redis;


import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;
import es.moki.ratelimitj.core.AsyncRateLimiter;
import es.moki.ratelimitj.core.LimitRule;
import es.moki.ratelimitj.core.RateLimiter;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static com.lambdaworks.redis.ScriptOutputType.VALUE;
import static java.util.Objects.requireNonNull;

@ThreadSafe
public class RedisSlidingWindowRateLimit implements AutoCloseable, AsyncRateLimiter, RateLimiter {

    private static final Logger LOG = LoggerFactory.getLogger(RedisSlidingWindowRateLimit.class);

    private final LimitRuleJsonSerialiser ruleSerialiser = new LimitRuleJsonSerialiser();
    private final RedisAsyncCommands<String, String> async;
    private final RedisScriptLoader scriptLoader;
    private final String rulesJson;
    private final boolean useRedisTime;

    // TODO Might require a configuration factory.

    public RedisSlidingWindowRateLimit(RedisClient redisClient, Set<LimitRule> rules) {
        this(redisClient, rules, false);
    }

    public RedisSlidingWindowRateLimit(RedisClient redisClient, Set<LimitRule> rules, boolean useRedisTime) {
        async = redisClient.connect().async();
        scriptLoader = new RedisScriptLoader(async, "sliding-window-ratelimit.lua");
        rulesJson = ruleSerialiser.encode(rules);
        this.useRedisTime = useRedisTime;
    }

    public CompletionStage<Boolean> overLimitAsync(String key) {
        return overLimitAsync(key, 1);
    }

    public CompletionStage<Boolean> overLimitAsync(String key, int weight) {
        requireNonNull(key);

        // TODO maybe load script completely async, but only useful the first time
        String sha = scriptLoader.scriptSha();

        // TODO seeing some strange behaviour
//         currentUnixTimeSeconds()
//                .thenApply(currentTime -> {
//                    LOG.debug("time {}", currentTime);
//                    return (CompletionStage) async.evalsha(sha, VALUE, new String[]{key}, rulesJson, currentTime, Integer.toString(weight));
//                }).thenApply(result -> {
//                    LOG.debug("result {}", result);
//                    return "1".equals(result);
//                });

        // TODO complete async use redis time
        return  async.evalsha(sha, VALUE, new String[]{key}, rulesJson, Long.toString(Instant.now().getEpochSecond()), Integer.toString(weight))
                .thenApply(result -> {
                    LOG.debug("result {}", result);
                    return "1".equals(result);
                });


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
            throw new RuntimeException("Failed to determine overLimit", e);
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

}
