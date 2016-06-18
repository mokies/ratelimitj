package es.moki.ratelimitj.dropwizard.app;


import com.google.common.collect.ImmutableSet;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import es.moki.ratelimij.dropwizard.RateLimitBundle;
import es.moki.ratelimitj.api.LimitRule;
import es.moki.ratelimitj.redis.RedisSlidingWindowRateLimiter;
import io.dropwizard.Application;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.concurrent.TimeUnit;

public class RateLimitApplication extends Application<RateLimitConfiguration> {

    private RedisClient client;
    private StatefulRedisConnection<String, String> connect;

    public void initialize(Bootstrap<RateLimitConfiguration> bootstrap) {
        client = RedisClient.create("redis://localhost");
        connect = client.connect();

        RedisSlidingWindowRateLimiter redisRateLimiter =
                new RedisSlidingWindowRateLimiter(connect, ImmutableSet.of(LimitRule.of(10, TimeUnit.SECONDS, 5)));
        bootstrap.addBundle(new RateLimitBundle<>(redisRateLimiter));
    }

    @Override
    public void run(RateLimitConfiguration configuration, Environment environment) throws Exception {

        environment.jersey().register(new LoginResource());


        // TODO make integration a little simpler
        environment.lifecycle().manage(new Managed() {
            @Override
            public void start() throws Exception {

            }

            @Override
            public void stop() throws Exception {
                connect.close();
                client.shutdown();
            }
        });
    }
}
