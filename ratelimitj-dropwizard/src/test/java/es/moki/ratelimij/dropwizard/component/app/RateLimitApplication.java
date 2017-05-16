package es.moki.ratelimij.dropwizard.component.app;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import es.moki.ratelimij.dropwizard.RateLimitBundle;
import es.moki.ratelimij.dropwizard.component.app.api.LoginResource;
import es.moki.ratelimij.dropwizard.component.app.api.UserResource;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiterFactory;
import es.moki.ratelimitj.redis.RedisRequestRateLimiterFactory;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class RateLimitApplication extends Application<Configuration> {

    private RedisClient redisClient;

    public void initialize(Bootstrap<Configuration> bootstrap) {
        redisClient = RedisClient.create("redis://localhost");
        RequestRateLimiterFactory factory = new RedisRequestRateLimiterFactory(redisClient);

        bootstrap.addBundle(new RateLimitBundle(factory));
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {

        environment.jersey().register(new LoginResource());
        environment.jersey().register(new UserResource());

        //TODO move this cleanup into the tests
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
