package es.moki.ratelimitj.redis.request;

import com.google.common.collect.ImmutableSet;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.redis.extensions.RedisStandaloneConnectionSetupExtension;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class RedisSlidingWindowRequestRateLimiterScriptLoadingTest {

    @RegisterExtension
    static RedisStandaloneConnectionSetupExtension extension = new RedisStandaloneConnectionSetupExtension();

    @Test
    void shouldRetryWhenScriptIfFlushed() {

        ImmutableSet<RequestLimitRule> rules = ImmutableSet.of(RequestLimitRule.of(Duration.ofSeconds(10), 5));
        RedisSlidingWindowRequestRateLimiter requestRateLimiter = new RedisSlidingWindowRequestRateLimiter(extension.getScriptingReactiveCommands(), extension.getKeyReactiveCommands(), rules);

        assertThat(requestRateLimiter.overLimitWhenIncremented("ip:127.0.1.1")).isFalse();

        extension.getScriptingReactiveCommands().scriptFlush().block();

        requestRateLimiter.overLimitWhenIncremented("ip:127.0.1.1");
    }
}