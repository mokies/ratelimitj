package es.moki.ratelimij.dropwizard;

import es.moki.ratelimij.dropwizard.filter.RateLimited429EnforcerFeature;
import es.moki.ratelimitj.core.api.RateLimiterFactory;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;
import org.glassfish.jersey.server.internal.inject.AbstractValueFactoryProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;

import javax.inject.Inject;
import javax.inject.Singleton;

import static java.util.Objects.requireNonNull;

public class RateLimitBundle implements ConfiguredBundle<Configuration> {

    private final RateLimiterFactory rateLimiterFactory;

    public RateLimitBundle(RateLimiterFactory rateLimiterFactory) {
        this.rateLimiterFactory = requireNonNull(rateLimiterFactory);
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }

    @Override
    public void run(final Configuration configuration,
                    final Environment environment) throws Exception {

        environment.jersey().register(new RateLimitingFactoryProvider.Binder(rateLimiterFactory));
        environment.jersey().register(new RateLimited429EnforcerFeature());

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

    @Singleton
    public static class RateLimitingFactoryProvider extends AbstractValueFactoryProvider {

        private RateLimiterFactory rateLimiterFactory;

        @Inject
        public RateLimitingFactoryProvider(final MultivaluedParameterExtractorProvider extractorProvider,
                                           final ServiceLocator injector,
                                           final RateLimiterFactoryProvider rateLimiterFactoryProvider) {
            super(extractorProvider, injector, Parameter.Source.UNKNOWN);
            this.rateLimiterFactory = rateLimiterFactoryProvider.factory;
        }

        @Override
        protected Factory<RateLimiterFactory> createValueFactory(final Parameter parameter) {
            final RateLimiting annotation = parameter.getAnnotation(RateLimiting.class);
            if (null == annotation) {
                return null;
            } else {
                return new AbstractContainerRequestValueFactory<RateLimiterFactory>() {
                    public RateLimiterFactory provide() {
                        return rateLimiterFactory;
                    }
                };
            }
        }

        public static class RateLimitingInjectionResolver extends ParamInjectionResolver<RateLimiting> {
            public RateLimitingInjectionResolver() {
                super(RateLimitingFactoryProvider.class);
            }
        }

        @Singleton
        public static class RateLimiterFactoryProvider {

            private final RateLimiterFactory factory;

            RateLimiterFactoryProvider(final RateLimiterFactory factory) {
                this.factory = factory;
            }
        }

        public static class Binder extends AbstractBinder {

            private final RateLimiterFactory rateLimiterFactory;

            public Binder(final RateLimiterFactory rateLimiterFactory) {
                this.rateLimiterFactory = rateLimiterFactory;
            }

            @Override
            protected void configure() {
                bind(new RateLimiterFactoryProvider(rateLimiterFactory)).to(RateLimiterFactoryProvider.class);
                bind(RateLimitingFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
                bind(RateLimitingFactoryProvider.RateLimitingInjectionResolver.class)
                        .to(new TypeLiteral<InjectionResolver<RateLimiting>>() {}).in(Singleton.class);
            }
        }
    }
}
