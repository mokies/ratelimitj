package com.touch.esp.common.dropwizard.ratelimit;

import com.google.common.collect.ImmutableSet;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.touch.esp.common.dropwizard.ratelimit.config.RateLimitBundleConfiguration;
import com.touch.esp.common.dropwizard.ratelimit.config.RateLimitConfiguration;
import com.touch.esp.common.dropwizard.ratelimit.filter.RateLimitFeature;
import es.moki.ratelimitj.redis.RedisSlidingWindowRateLimiter;
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

public class RateLimitBundle implements ConfiguredBundle<RateLimitBundleConfiguration> {

    public RateLimitBundle() {
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }

    @Override
    public void run(final RateLimitBundleConfiguration configuration,
                    final Environment environment) throws Exception {

        final RateLimitConfiguration config = configuration.getRateLimitConfiguration();
        final String host = config.getHost();
        final Integer port = config.getPort();

        final RedisClient client = RedisClient.create("redis://" + host + ":" + port);
        final StatefulRedisConnection<String, String> connect = client.connect();

        environment.jersey().register(new RateLimitingFactoryProvider.Binder(connect));
        environment.jersey().register(new RateLimitFeature());

        environment.lifecycle().manage(new Managed() {
            @Override
            public void start() throws Exception {
            }

            @Override
            public void stop() throws Exception {
                connect.close();
                client.shutdown();
            }
        });
    }

    @Singleton
    public static class RateLimitingFactoryProvider extends AbstractValueFactoryProvider {

        private StatefulRedisConnection<String, String>  connection;

        @Inject
        public RateLimitingFactoryProvider(final MultivaluedParameterExtractorProvider extractorProvider,
                                           final ServiceLocator injector,
                                           final ConnectionProvider connectionProvider) {
            super(extractorProvider, injector, Parameter.Source.UNKNOWN);
            this.connection = connectionProvider.connection;
        }

        @Override
        protected Factory<?> createValueFactory(final Parameter parameter) {
            final RateLimiting annotation = parameter.getAnnotation(RateLimiting.class);
            if (null == annotation) {
                return null;
            } else {
                return new AbstractContainerRequestValueFactory<RateLimitFactory>() {
                    public RateLimitFactory provide() {
                        return (limitRules) -> new RedisSlidingWindowRateLimiter(connection, ImmutableSet.copyOf(limitRules));
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
        public static class ConnectionProvider {

            private final StatefulRedisConnection<String, String>  connection;

            ConnectionProvider(final StatefulRedisConnection<String, String> connection) {
                this.connection = connection;
            }
        }

        public static class Binder extends AbstractBinder {

            private final StatefulRedisConnection<String, String> connection;

            public Binder(final StatefulRedisConnection<String, String> connection) {
                this.connection = connection;
            }

            @Override
            protected void configure() {
                bind(new ConnectionProvider(connection)).to(ConnectionProvider.class);
                bind(RateLimitingFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
                bind(RateLimitingFactoryProvider.RateLimitingInjectionResolver.class).to(
                        new TypeLiteral<InjectionResolver<RateLimiting>>() {
                        }
                ).in(Singleton.class);
            }
        }
    }
}
