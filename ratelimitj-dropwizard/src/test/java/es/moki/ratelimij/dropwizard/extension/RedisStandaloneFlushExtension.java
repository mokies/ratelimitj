package es.moki.ratelimitj.redis.extensions;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class RedisStandaloneFlushExtension implements
        BeforeAllCallback, AfterAllCallback, AfterEachCallback {

    private static RedisClient client;
    private static StatefulRedisConnection<String, String> connect;

    @Override
    @SuppressWarnings("FutureReturnValueIgnored")
    public void afterAll(ExtensionContext context) {
        client.shutdownAsync();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        connect.sync().flushdb();
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        client = RedisClient.create("redis://localhost:7006");
        connect = client.connect();
    }

}