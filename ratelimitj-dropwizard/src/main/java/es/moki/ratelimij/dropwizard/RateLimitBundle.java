package es.moki.ratelimij.dropwizard;

import es.moki.ratelimij.dropwizard.filter.RateLimited429EnforcerFeature;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiterFactory;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import static java.util.Objects.requireNonNull;

public class RateLimitBundle implements ConfiguredBundle<Configuration> {

    private final RequestRateLimiterFactory requestRateLimiterFactory;

    public RateLimitBundle(RequestRateLimiterFactory requestRateLimiterFactory) {
        this.requestRateLimiterFactory = requireNonNull(requestRateLimiterFactory);
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }

    @Override
    public void run(final Configuration configuration, final Environment environment) {
        environment.jersey().register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(requestRateLimiterFactory).to(RequestRateLimiterFactory.class);
            }
        });
        environment.jersey().register(new RateLimited429EnforcerFeature());
        environment.lifecycle().manage(new Managed() {
            @Override
            public void start() {
            }

            @Override
            public void stop() throws Exception {
                requestRateLimiterFactory.close();
            }
        });
    }
}
