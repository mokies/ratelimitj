package es.moki.ratelimij.dropwizard;


import es.moki.ratelimij.dropwizard.filter.RateLimitFilter;
import es.moki.ratelimitj.core.RateLimiter;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class RateLimitBundle<T extends Configuration> implements ConfiguredBundle<T> {

    private final RateLimiter rateLimit;

    public RateLimitBundle(RateLimiter rateLimit) {
        this.rateLimit = rateLimit;
    }

    @Override
    public void run(T configuration, Environment environment) throws Exception {


        environment.lifecycle().manage(new Managed() {
        			@Override
        			public void start() throws Exception {
        			}

        			@Override
        			public void stop() throws Exception {
                        rateLimit.close();
        			}
        		});

        // TODO provide decoupled mechanism to bind ratelimit implementation to dropwizard

        RateLimitFilter rateLimitFilter = new RateLimitFilter(rateLimit);
        environment.jersey().register(rateLimitFilter);

        // TODO configure via jersey feature

        //environment.jersey().register(DateRequiredFeature.class);
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }
}
