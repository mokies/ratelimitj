package es.moki.ratelimij.dropwizard.component.app;

import es.moki.ratelimij.dropwizard.RateLimitBundle;
import es.moki.ratelimij.dropwizard.component.app.api.LoginResource;
import es.moki.ratelimij.dropwizard.component.app.api.TrekResource;
import es.moki.ratelimij.dropwizard.component.app.api.UserResource;
import es.moki.ratelimij.dropwizard.component.app.auth.TestOAuthAuthenticator;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiterFactory;
import es.moki.ratelimitj.redis.request.RedisRateLimiterFactory;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.PrincipalImpl;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.lettuce.core.RedisClient;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

public class RateLimitApplication extends Application<Configuration> {

    private RedisClient redisClient;

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        redisClient = RedisClient.create("redis://localhost:7006");
        RequestRateLimiterFactory factory = new RedisRateLimiterFactory(redisClient);

        //RequestRateLimiterFactory factory = new InMemoryRateLimiterFactory();

        bootstrap.addBundle(new RateLimitBundle(factory));
    }

    @Override
    public void run(Configuration configuration, Environment environment) {

        environment.jersey().register(new LoginResource());
        environment.jersey().register(new UserResource());
        environment.jersey().register(new TrekResource());

        environment.jersey().register(new AuthDynamicFeature(
                new OAuthCredentialAuthFilter.Builder<PrincipalImpl>()
                        .setAuthenticator(new TestOAuthAuthenticator()).setPrefix("Bearer")
                        .buildAuthFilter()));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(PrincipalImpl.class));
    }
}
