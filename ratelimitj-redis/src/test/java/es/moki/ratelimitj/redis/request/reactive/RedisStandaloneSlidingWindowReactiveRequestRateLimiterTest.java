package es.moki.ratelimitj.redis.request.reactive;

import es.moki.ratelimitj.redis.extensions.RedisStandaloneConnectionSetupExtension;
import io.lettuce.core.api.reactive.RedisKeyReactiveCommands;
import io.lettuce.core.api.reactive.RedisScriptingReactiveCommands;
import org.junit.jupiter.api.extension.RegisterExtension;

public class RedisStandaloneSlidingWindowReactiveRequestRateLimiterTest extends RedisSlidingWindowReactiveRequestRateLimiterTest {
    @RegisterExtension
    static RedisStandaloneConnectionSetupExtension extension = new RedisStandaloneConnectionSetupExtension();

    @Override
    RedisScriptingReactiveCommands<String, String> getRedisScriptingReactiveCommands() {
        return extension.getScriptingReactiveCommands();
    }

    @Override
    RedisKeyReactiveCommands<String, String> getRedisKeyReactiveCommands() {
        return extension.getKeyReactiveCommands();
    }
}
