package es.moki.ratelimitj.redis;


import com.lambdaworks.redis.api.StatefulRedisConnection;
import es.moki.ratelimitj.core.limiter.request.AsyncRequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.ReactiveRequestRateLimiter;
import es.moki.ratelimitj.core.time.SystemTimeSupplier;
import es.moki.ratelimitj.core.time.TimeSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static com.lambdaworks.redis.ScriptOutputType.VALUE;
import static java.util.Objects.requireNonNull;

@ThreadSafe
public class RedisSlidingWindowRequestRateLimiter implements RequestRateLimiter, AsyncRequestRateLimiter, ReactiveRequestRateLimiter {

    private static final Logger LOG = LoggerFactory.getLogger(RedisSlidingWindowRequestRateLimiter.class);

    private final LimitRuleJsonSerialiser serialiser = new LimitRuleJsonSerialiser();

    private final StatefulRedisConnection<String, String> connection;
    private final RedisScriptLoader scriptLoader;
    private final String rulesJson;
    private final TimeSupplier timeSupplier;

    public RedisSlidingWindowRequestRateLimiter(StatefulRedisConnection<String, String> connection, Set<RequestLimitRule> rules) {
        this(connection, rules, new SystemTimeSupplier());
    }

    public RedisSlidingWindowRequestRateLimiter(StatefulRedisConnection<String, String> connection, Set<RequestLimitRule> rules, TimeSupplier timeSupplier) {
        this.connection = connection;
        scriptLoader = new RedisScriptLoader(connection, "sliding-window-ratelimit.lua");
        rulesJson = serialiserLimitRules(rules);
        this.timeSupplier = timeSupplier;
    }

    private String serialiserLimitRules(Set<RequestLimitRule> rules) {
        return serialiser.encode(rules);
    }

    public CompletionStage<Boolean> overLimitAsync(String key) {
        return overLimitAsync(key, 1);
    }

    // TODO support multi keys
    public CompletionStage<Boolean> overLimitAsync(String key, int weight) {
        requireNonNull(key);

        LOG.debug("overLimit for key '{}' of weight {}", key, weight);

        String sha = scriptLoader.scriptSha();

        return timeSupplier.getAsync().thenCompose(time ->
                connection.async().evalsha(sha, VALUE, new String[]{key}, rulesJson, Long.toString(time), Integer.toString(weight))
        ).thenApply(result -> {
            boolean overLimit = "1".equals(result);
            LOG.debug("over limit {}", overLimit);
            return overLimit;
        });

        // TODO handle scenario where script is not loaded, flush scripts and test scenario
    }

    @Override
    public CompletionStage<Boolean> resetLimitAsync(String key) {
        return connection.async().del(key).thenApply(result -> 1 == result);
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
    public boolean resetLimit(String key) {
        try {
            return resetLimitAsync(key).toCompletableFuture().get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to resetLimit", e);
        }
    }

    @Override
    public Mono<Boolean> overLimitReactive(String key) {
        return Mono.fromFuture(overLimitAsync(key).toCompletableFuture());
    }

    @Override
    public Mono<Boolean> overLimitReactive(String key, int weight) {
        return Mono.fromFuture(overLimitAsync(key, weight).toCompletableFuture());
    }

    @Override
    public Mono<Boolean> resetLimitReactive(String key) {
        return Mono.fromFuture(resetLimitAsync(key).toCompletableFuture());
    }


}
