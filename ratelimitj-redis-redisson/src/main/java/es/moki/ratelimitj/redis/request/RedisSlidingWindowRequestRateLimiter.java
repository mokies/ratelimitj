package es.moki.ratelimitj.redis.request;


import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.ThreadSafe;

import es.moki.ratelimitj.core.limiter.request.ReactiveRequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRulesSupplier;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.core.time.SystemTimeSupplier;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.redis.request.RedisScriptLoader.StoredScript;
import org.redisson.api.RKeysReactive;
import org.redisson.api.RScript;
import org.redisson.api.RScriptReactive;
import org.redisson.client.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import static java.util.Objects.requireNonNull;

@ParametersAreNonnullByDefault
@ThreadSafe
public class RedisSlidingWindowRequestRateLimiter implements RequestRateLimiter, ReactiveRequestRateLimiter {

    private static final Logger LOG = LoggerFactory.getLogger(RedisSlidingWindowRequestRateLimiter.class);

    private static final Duration BLOCK_TIMEOUT = Duration.of(5, ChronoUnit.SECONDS);

    private static final Predicate<Throwable> STARTS_WITH_NO_SCRIPT_ERROR = e -> e instanceof RedisException; //TODO look for better error RedisNoScriptException or create one


    private final RScriptReactive redisScriptingReactiveCommands;
    private final RKeysReactive redisKeyCommands;
    private final RedisScriptLoader scriptLoader;
    private final RequestLimitRulesSupplier<String> requestLimitRulesSupplier;
    private final TimeSupplier timeSupplier;

    public RedisSlidingWindowRequestRateLimiter(RScriptReactive redisScriptingReactiveCommands, RKeysReactive redisKeyCommands, RequestLimitRule rule) {
        this(redisScriptingReactiveCommands, redisKeyCommands, Collections.singleton(rule));
    }

    public RedisSlidingWindowRequestRateLimiter(RScriptReactive redisScriptingReactiveCommands, RKeysReactive redisKeyCommands, Set<RequestLimitRule> rules) {
        this(redisScriptingReactiveCommands, redisKeyCommands, rules, new SystemTimeSupplier());
    }

    public RedisSlidingWindowRequestRateLimiter(RScriptReactive redisScriptingReactiveCommands, RKeysReactive redisKeyCommands, Set<RequestLimitRule> rules, TimeSupplier timeSupplier) {
        requireNonNull(rules, "rules can not be null");
        requireNonNull(timeSupplier, "time supplier can not be null");
        requireNonNull(redisScriptingReactiveCommands, "redisScriptingReactiveCommands can not be null");
        requireNonNull(redisKeyCommands, "redisKeyCommands can not be null");
        this.redisScriptingReactiveCommands = redisScriptingReactiveCommands;
        this.redisKeyCommands = redisKeyCommands;
        scriptLoader = new RedisScriptLoader(redisScriptingReactiveCommands, "sliding-window-ratelimit.lua");
        requestLimitRulesSupplier = new SerializedRequestLimitRulesSupplier(rules);
        this.timeSupplier = timeSupplier;
    }

    @Override
    public boolean overLimitWhenIncremented(String key) {
        return overLimitWhenIncremented(key, 1);
    }

    @Override
    public boolean overLimitWhenIncremented(String key, int weight) {
        return throwOnTimeout(eqOrGeLimitReactive(key, weight, true));
    }

    @Override
    public boolean geLimitWhenIncremented(String key) {
        return geLimitWhenIncremented(key, 1);
    }

    @Override
    public boolean geLimitWhenIncremented(String key, int weight) {
        return throwOnTimeout(eqOrGeLimitReactive(key, weight, false));
    }

//    @Override
//    public boolean isOverLimit(String key) {
//        return overLimitWhenIncremented(key, 0);
//    }
//
//    @Override
//    public boolean isGeLimit(String key) {
//        return geLimitWhenIncremented(key, 0);
//    }

    @Override
    public boolean resetLimit(String key) {
        return throwOnTimeout(resetLimitReactive(key));
    }

    @Override
    public Mono<Boolean> overLimitWhenIncrementedReactive(String key) {
        return overLimitWhenIncrementedReactive(key, 1);
    }

    @Override
    public Mono<Boolean> overLimitWhenIncrementedReactive(String key, int weight) {
        return eqOrGeLimitReactive(key, weight, true);
    }

    @Override
    public Mono<Boolean> geLimitWhenIncrementedReactive(String key) {
        return geLimitWhenIncrementedReactive(key, 1);
    }

    @Override
    public Mono<Boolean> geLimitWhenIncrementedReactive(String key, int weight) {
        return eqOrGeLimitReactive(key, weight, false);
    }

    @Override
    public Mono<Boolean> resetLimitReactive(String key) {
        return redisKeyCommands.delete(key).map(count -> count > 0);
    }

    private Mono<Boolean> eqOrGeLimitReactive(String key, int weight, boolean strictlyGreater) {
        requireNonNull(key);
        String rulesJson = requestLimitRulesSupplier.getRules(key);

        return Mono.zip(timeSupplier.getReactive(), scriptLoader.storedScript())
                .flatMapMany(tuple -> {
                    Long time = tuple.getT1();
                    StoredScript script = tuple.getT2();
                    return redisScriptingReactiveCommands
                            .evalSha(RScript.Mode.READ_ONLY, script.getSha(), RScript.ReturnType.VALUE, Arrays.asList(new String[]{key}),rulesJson, time.toString(), Integer.toString(weight), toStringOneZero(strictlyGreater))
                            .doOnError(STARTS_WITH_NO_SCRIPT_ERROR, e -> script.dispose());
                })
                .retry(1, STARTS_WITH_NO_SCRIPT_ERROR)
                .single()
                .map("1"::equals)
                .doOnSuccess(over -> {
                    if (over) {
                        LOG.debug("Requests matched by key '{}' incremented by weight {} are greater than {}the limit", key, weight, strictlyGreater ? "" : "or equal to ");
                    }
                });
    }

    private String toStringOneZero(boolean strictlyGreater) {
        return strictlyGreater ? "1" : "0";
    }

    private boolean throwOnTimeout(Mono<Boolean> mono) {
        Boolean result = mono.block(BLOCK_TIMEOUT);
        if (result == null) {
            throw new RuntimeException("waited " + BLOCK_TIMEOUT + "before timing out");
        }
        return result;
    }

}
