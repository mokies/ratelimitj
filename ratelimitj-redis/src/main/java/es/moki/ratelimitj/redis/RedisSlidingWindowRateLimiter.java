package es.moki.ratelimitj.redis;


import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;
import es.moki.ratelimitj.api.AsyncRateLimiter;
import es.moki.ratelimitj.api.LimitRule;
import es.moki.ratelimitj.api.RateLimiter;
import es.moki.ratelimitj.core.time.time.SystemTimeSupplier;
import es.moki.ratelimitj.core.time.time.TimeSupplier;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static com.lambdaworks.redis.ScriptOutputType.VALUE;
import static java.util.Objects.requireNonNull;

@ThreadSafe
public class RedisSlidingWindowRateLimiter implements AsyncRateLimiter, RateLimiter {

    private static final Logger LOG = LoggerFactory.getLogger(RedisSlidingWindowRateLimiter.class);

    private final RedisAsyncCommands<String, String> async;
    private final RedisScriptLoader scriptLoader;
    private final String rulesJson;
    private final TimeSupplier timeSupplier;

    public RedisSlidingWindowRateLimiter(StatefulRedisConnection<String, String> connection, Set<LimitRule> rules) {
        this(connection, rules, new SystemTimeSupplier());
    }

    public RedisSlidingWindowRateLimiter(StatefulRedisConnection<String, String> connection, Set<LimitRule> rules, TimeSupplier timeSupplier) {
        async = connection.async();
        scriptLoader = new RedisScriptLoader(connection, "sliding-window-ratelimit.lua");
        rulesJson = serialiserLimitRules(rules);
        this.timeSupplier = timeSupplier;
    }

    private String serialiserLimitRules(Set<LimitRule> rules) {
        LimitRuleJsonSerialiser ruleSerialiser = new LimitRuleJsonSerialiser();
        return ruleSerialiser.encode(rules);
    }

    public CompletionStage<Boolean> overLimitAsync(String key) {
        return overLimitAsync(key, 1);
    }

    // TODO support multi keys
    public CompletionStage<Boolean> overLimitAsync(String key, int weight) {
        requireNonNull(key);

        LOG.debug("overLimitAsync for key '{}' of weight {}", key, weight);

        String sha = scriptLoader.scriptSha();

        // TODO currently blocking, use async redis time
        Long timeSeconds = timeSupplier.get();

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
        return async.evalsha(sha, VALUE, new String[]{key}, rulesJson, Long.toString(timeSeconds), Integer.toString(weight))
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
}
