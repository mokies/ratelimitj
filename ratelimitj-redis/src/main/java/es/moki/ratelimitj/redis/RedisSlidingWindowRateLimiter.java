package es.moki.ratelimitj.redis;


import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;
import es.moki.ratelimitj.core.api.AsyncRateLimiter;
import es.moki.ratelimitj.core.api.LimitRule;
import es.moki.ratelimitj.core.api.RateLimiter;
import es.moki.ratelimitj.core.api.ReactiveRateLimiter;
import es.moki.ratelimitj.core.time.SystemTimeSupplier;
import es.moki.ratelimitj.core.time.TimeSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static com.lambdaworks.redis.ScriptOutputType.VALUE;
import static java.util.Objects.requireNonNull;
import static net.javacrumbs.futureconverter.java8rx.FutureConverter.toObservable;

@ThreadSafe
public class RedisSlidingWindowRateLimiter implements RateLimiter, AsyncRateLimiter, ReactiveRateLimiter {

    private static final Logger LOG = LoggerFactory.getLogger(RedisSlidingWindowRateLimiter.class);

    private final LimitRuleJsonSerialiser serialiser = new LimitRuleJsonSerialiser();

    private final RedisAsyncCommands<String, String> async;
    private final RedisScriptLoader scriptLoader;
    private final String rulesJson;
    private final TimeSupplier timeSupplier;

    public RedisSlidingWindowRateLimiter(StatefulRedisConnection<String, String> connection, Set<LimitRule> rules) {
        this(connection, rules, new SystemTimeSupplier());
    }

    public RedisSlidingWindowRateLimiter(StatefulRedisConnection<String, String> connection, Set<LimitRule> rules, TimeSupplier timeSupplier) {
        async = connection.async();
        connection.reactive();
        scriptLoader = new RedisScriptLoader(connection, "sliding-window-ratelimit.lua");
        rulesJson = serialiserLimitRules(rules);
        this.timeSupplier = timeSupplier;
    }

    private String serialiserLimitRules(Set<LimitRule> rules) {
        return serialiser.encode(rules);
    }

    public CompletionStage<Boolean> overLimitAsync(String key) {
        return overLimitAsync(key, 1);
    }

    // TODO support multi keys
    public CompletionStage<Boolean> overLimitAsync(String key, int weight) {
        requireNonNull(key);

        LOG.debug("overLimitAsync for key '{}' of weight {}", key, weight);

        String sha = scriptLoader.scriptSha();

        return timeSupplier.getAsync().thenCompose(time ->
                async.evalsha(sha, VALUE, new String[]{key}, rulesJson, Long.toString(time), Integer.toString(weight))
        ).thenApply(result -> {
            boolean overLimit = "1".equals(result);
            LOG.debug("over limit {}", overLimit);
            return overLimit;
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

    @Override
    public Observable<Boolean> overLimitReactive(String key) {
        return toObservable(overLimitAsync(key).toCompletableFuture());
    }

    @Override
    public Observable<Boolean> overLimitReactive(String key, int weight) {
        return toObservable(overLimitAsync(key, weight).toCompletableFuture());
    }
}
