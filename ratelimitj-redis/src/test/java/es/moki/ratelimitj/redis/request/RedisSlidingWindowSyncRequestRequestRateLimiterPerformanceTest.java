package es.moki.ratelimitj.redis.request;

import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.redis.extensions.RedisStandaloneConnectionSetupExtension;
import es.moki.ratelimitj.test.limiter.request.AbstractSyncRequestRateLimiterPerformanceTest;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Set;


public class RedisSlidingWindowSyncRequestRequestRateLimiterPerformanceTest extends AbstractSyncRequestRateLimiterPerformanceTest {

    @RegisterExtension
    static RedisStandaloneConnectionSetupExtension extension = new RedisStandaloneConnectionSetupExtension();

    @Override
    protected RequestRateLimiter getRateLimiter(Set<RequestLimitRule> rules, TimeSupplier timeSupplier) {
        return new RedisSlidingWindowRequestRateLimiter(extension.getScriptingReactiveCommands(), extension.getKeyReactiveCommands(), rules, timeSupplier);
    }
}
