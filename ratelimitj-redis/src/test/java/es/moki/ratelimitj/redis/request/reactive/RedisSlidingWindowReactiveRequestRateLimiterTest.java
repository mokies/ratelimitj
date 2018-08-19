package es.moki.ratelimitj.redis.request.reactive;

import com.google.common.collect.ImmutableSet;
import es.moki.ratelimitj.core.limiter.request.ReactiveRequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.time.SystemTimeSupplier;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.redis.request.RedisSlidingWindowRequestRateLimiter;
import es.moki.ratelimitj.test.limiter.request.AbstractReactiveRequestRateLimiterTest;
import io.lettuce.core.api.reactive.RedisKeyReactiveCommands;
import io.lettuce.core.api.reactive.RedisScriptingReactiveCommands;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class RedisSlidingWindowReactiveRequestRateLimiterTest extends AbstractReactiveRequestRateLimiterTest {

    abstract RedisScriptingReactiveCommands<String, String> getRedisScriptingReactiveCommands();

    abstract RedisKeyReactiveCommands<String, String> getRedisKeyReactiveCommands();

    @Override
    protected ReactiveRequestRateLimiter getRateLimiter(Set<RequestLimitRule> rules, TimeSupplier timeSupplier) {
        return new RedisSlidingWindowRequestRateLimiter(getRedisScriptingReactiveCommands(), getRedisKeyReactiveCommands(), rules, timeSupplier);
    }

    @Test
    void shouldReloadMissingScript() {
        ImmutableSet<RequestLimitRule> rules = ImmutableSet.of(RequestLimitRule.of(Duration.ofSeconds(60), 1));
        ReactiveRequestRateLimiter rateLimiter = getRateLimiter(rules, new SystemTimeSupplier());

        rateLimiter.overLimitWhenIncrementedReactive(UUID.randomUUID().toString()).block(Duration.ofSeconds(5));

        getRedisScriptingReactiveCommands().scriptFlush().block();

        rateLimiter.overLimitWhenIncrementedReactive(UUID.randomUUID().toString()).block(Duration.ofSeconds(5));
    }
}
