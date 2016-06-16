package es.moki.ratelimitj.redis.time;


import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;

import java.util.concurrent.CompletionStage;

public class RedisTimeSupplier implements TimeSupplier {

    private final RedisAsyncCommands<String, String> async;

    public RedisTimeSupplier(StatefulRedisConnection<String, String> connection) {
        this.async = connection.async();
    }
    @Override
    public CompletionStage<Long> get() {
        return async.time().thenApply(strings -> Long.parseLong(strings.get(0)));
    }
}
