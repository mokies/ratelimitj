package es.moki.ratelimitj.redis.extensions;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisKeyReactiveCommands;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.api.reactive.RedisScriptingReactiveCommands;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.reactive.RedisAdvancedClusterReactiveCommands;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class RedisClusterConnectionSetupExtension implements
        BeforeAllCallback, AfterAllCallback, AfterEachCallback {

    private static RedisClusterClient client;
    private static StatefulRedisClusterConnection<String, String> connect;
    private static RedisAdvancedClusterReactiveCommands<String, String> reactiveCommands;

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
        client = RedisClusterClient.create("redis://localhost:7000");
        connect = client.connect();
        reactiveCommands = connect.reactive();
    }

    public RedisClusterClient getClient() {
        return client;
    }

    public RedisScriptingReactiveCommands<String, String> getRedisScriptingReactiveCommands() {
        return reactiveCommands;
    }

    public RedisKeyReactiveCommands<String, String> getRedisKeyReactiveCommands() {
        return reactiveCommands;
    }

}