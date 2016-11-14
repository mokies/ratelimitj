package es.moki.ratelimij.dropwizard;


import es.moki.ratelimij.dropwizard.filter.RateLimitFeature;
import es.moki.ratelimitj.core.api.RateLimiterFactory;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class RateLimitBundle<T extends Configuration> implements ConfiguredBundle<T> {

    public static final String PROPERTY_FACTORY = "rateLimiterFactory";

    private final RateLimiterFactory rateLimiterFactory;

    public RateLimitBundle(RateLimiterFactory rateLimiterFactory) {
        this.rateLimiterFactory = rateLimiterFactory;
    }

    @Override
    public void run(T configuration, Environment environment) throws Exception {

//        SimpleRateLimitFilter rateLimitFilter = new SimpleRateLimitFilter(rateLimit);


        environment.jersey().property(PROPERTY_FACTORY, rateLimiterFactory);
        environment.jersey().register(RateLimitFeature.class);

        environment.lifecycle().manage(new Managed() {
            @Override
            public void start() throws Exception {

            }

            @Override
            public void stop() throws Exception {
                rateLimiterFactory.close();
            }
        });
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }
}
