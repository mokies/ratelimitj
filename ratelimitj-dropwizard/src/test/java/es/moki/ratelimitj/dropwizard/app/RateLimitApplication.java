package es.moki.ratelimitj.dropwizard.app;


import com.lambdaworks.redis.RedisClient;
import es.moki.ratelimij.dropwizard.RateLimitBundle;
import es.moki.ratelimitj.core.api.RateLimiterFactory;
import es.moki.ratelimitj.redis.RedisRateLimiterFactory;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class RateLimitApplication extends Application<RateLimitConfiguration> {


    public void initialize(Bootstrap<RateLimitConfiguration> bootstrap) {

        RateLimiterFactory rateLimiterFactory = new RedisRateLimiterFactory(RedisClient.create("redis://localhost"));

        bootstrap.addBundle(new RateLimitBundle<>(rateLimiterFactory));
    }

    @Override
    public void run(RateLimitConfiguration configuration, Environment environment) throws Exception {

        environment.jersey().register(new LoginResource());


    }
}
