package es.moki.ratelimitj.redis.request.reactive;

import es.moki.ratelimitj.redis.extensions.RedisClusterConnectionSetupExtension;
import io.lettuce.core.api.reactive.RedisKeyReactiveCommands;
import io.lettuce.core.api.reactive.RedisScriptingReactiveCommands;
import org.junit.jupiter.api.extension.RegisterExtension;

public class RedisClusterSlidingWindowReactiveRequestRateLimiterTest extends RedisSlidingWindowReactiveRequestRateLimiterTest {

    @RegisterExtension
    static RedisClusterConnectionSetupExtension extension = new RedisClusterConnectionSetupExtension();

    @Override
    RedisScriptingReactiveCommands<String, String> getRedisScriptingReactiveCommands() {
        return extension.getRedisScriptingReactiveCommands();
    }

    @Override
    RedisKeyReactiveCommands<String, String> getRedisKeyReactiveCommands() {
        return extension.getRedisKeyReactiveCommands();
    }

}
