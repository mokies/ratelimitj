package es.moki.ratelimij.dropwizard.component.app;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import es.moki.ratelimij.dropwizard.RateLimitBundle;
import es.moki.ratelimij.dropwizard.component.app.api.UserResource;
import es.moki.ratelimij.dropwizard.component.app.api.LoginResource;
import es.moki.ratelimij.dropwizard.component.app.config.RateLimitApplicationConfiguration;
import es.moki.ratelimitj.core.api.RateLimiterFactory;
import es.moki.ratelimitj.redis.RedisRateLimiterFactory;
import io.dropwizard.Application;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class RateLimitApplication extends Application<RateLimitApplicationConfiguration> {

    private RedisClient redisClient;

    public void initialize(Bootstrap<RateLimitApplicationConfiguration> bootstrap) {
        redisClient = RedisClient.create("redis://localhost");
        RateLimiterFactory factory = new RedisRateLimiterFactory(redisClient);

        bootstrap.addBundle(new RateLimitBundle(factory));
    }

    @Override
    public void run(RateLimitApplicationConfiguration configuration, Environment environment) throws Exception {

        environment.jersey().register(new LoginResource());
        environment.jersey().register(new UserResource());


        environment.lifecycle().manage(new Managed() {
            @Override
            public void start() throws Exception {
            }

            @Override
            public void stop() throws Exception {
                flushRedis();
            }

            private void flushRedis() {
                try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
                    connection.sync().flushdb();
                }
                redisClient.shutdown();
            }
        });

    }
}
