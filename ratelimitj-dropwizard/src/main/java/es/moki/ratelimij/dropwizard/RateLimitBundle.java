package es.moki.ratelimij.dropwizard;

import es.moki.ratelimij.dropwizard.filter.RateLimited429EnforcerFeature;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiterFactory;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Function;

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
    public void run(final Configuration configuration,
                    final Environment environment) {

        environment.jersey().register(new RateLimitingFactoryProvider.Binder(requestRateLimiterFactory));
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

    @Singleton
    public static class RateLimitingFactoryProvider implements ValueParamProvider {

        private RequestRateLimiterFactory requestRateLimiterFactory;

        @Inject
        public RateLimitingFactoryProvider(final RateLimiterFactoryProvider rateLimiterFactoryProvider) {
            this.requestRateLimiterFactory = rateLimiterFactoryProvider.factory;
        }

        @Override
        public Function<ContainerRequest, RequestRateLimiterFactory> getValueProvider(Parameter parameter) {
            final RateLimiting annotation = parameter.getAnnotation(RateLimiting.class);
            if (null == annotation) {
                return null;
            }
            return containerRequest -> requestRateLimiterFactory;
        }

        @Override
        public PriorityType getPriority() {
            return Priority.NORMAL;
        }

        @Singleton
        public static class RateLimiterFactoryProvider {

            private final RequestRateLimiterFactory factory;

            RateLimiterFactoryProvider(final RequestRateLimiterFactory factory) {
                this.factory = factory;
            }
        }

        public static class Binder extends AbstractBinder {

            private final RequestRateLimiterFactory requestRateLimiterFactory;

            public Binder(final RequestRateLimiterFactory requestRateLimiterFactory) {
                this.requestRateLimiterFactory = requestRateLimiterFactory;
            }

            @Override
            protected void configure() {
                bind(new RateLimiterFactoryProvider(requestRateLimiterFactory)).to(RateLimiterFactoryProvider.class);
                bind(RateLimitingFactoryProvider.class).to(ValueParamProvider.class).in(Singleton.class);
            }

        }
    }

}
