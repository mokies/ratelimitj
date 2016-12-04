package com.touch.esp.common.dropwizard.ratelimit.app;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.touch.esp.common.dropwizard.ratelimit.RateLimitBundle;
import com.touch.esp.common.dropwizard.ratelimit.app.api.LoginResource;
import com.touch.esp.common.dropwizard.ratelimit.app.api.UserResource;
import com.touch.esp.common.dropwizard.ratelimit.app.config.RateLimitApplicationConfiguration;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class RateLimitApplication extends Application<RateLimitApplicationConfiguration> {

    private RedisClient client;
    private StatefulRedisConnection<String, String> connect;

    public void initialize(Bootstrap<RateLimitApplicationConfiguration> bootstrap) {
        bootstrap.addBundle(new RateLimitBundle());
    }

    @Override
    public void run(RateLimitApplicationConfiguration configuration, Environment environment) throws Exception {

        environment.jersey().register(new LoginResource());
        environment.jersey().register(new UserResource());

    }
}
