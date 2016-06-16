package es.moki.ratelimitj.redis.time;


import com.lambdaworks.redis.api.StatefulRedisConnection;
import es.moki.ratelimitj.core.time.time.TimeSupplier;

import java.util.concurrent.CompletionStage;

public class RedisTimeSupplier implements TimeSupplier {

    private final StatefulRedisConnection<String, String> connection;

    public RedisTimeSupplier(StatefulRedisConnection<String, String> connection) {
        this.connection = connection;
    }

    @Override
    public CompletionStage<Long> getAsync() {
        return connection.async().time().thenApply(strings -> Long.parseLong(strings.get(0)));
    }

    @Override
    public long get() {
        return Long.parseLong(connection.sync().time().get(0));
    }
}
