package com.touch.esp.common.dropwizard.ratelimit.filter;

import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.touch.esp.common.dropwizard.ratelimit.RateLimitFactory;
import com.touch.esp.common.dropwizard.ratelimit.RateLimiting;
import es.moki.ratelimitj.core.api.RateLimiter;
import io.dropwizard.testing.junit.ResourceTestRule;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class RateLimitFilterTest {

    @Rule
    public ResourceTestRule rule = ResourceTestRule
            .builder()
            .addProvider(new TestRateLimitingFactoryProvider.Binder())
            .addProvider(new RateLimitFeature())
            .addResource(new TestResource())
            .build();
    @Mock
    private StatefulRedisConnection<String, String> connection;

    @Test
    public void shouldAdd() {
        final Response response = rule.getJerseyTest().target("/test/{id}").resolveTemplate("id", 1)
                                      .request(MediaType.APPLICATION_JSON_TYPE)
                                      .get();

        assertThat(response.getStatus()).isEqualTo(200);

    }

    @Singleton
    public static class TestRateLimitingFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        public TestRateLimitingFactoryProvider(final MultivaluedParameterExtractorProvider extractorProvider,
                                               final ServiceLocator injector) {
            super(extractorProvider, injector, Parameter.Source.UNKNOWN);
        }

        @Override
        protected Factory<?> createValueFactory(final Parameter parameter) {
            final RateLimiting annotation = parameter.getAnnotation(RateLimiting.class);
            if (null == annotation) {
                return null;
            } else {
                return new AbstractContainerRequestValueFactory<RateLimitFactory>() {
                    public RateLimitFactory provide() {
                        return (limitRules) -> new RateLimiter() {
                            @Override
                            public boolean overLimit(final String key) {
                                return false;
                            }

                            @Override
                            public boolean overLimit(final String key,
                                                     final int weight) {
                                return false;
                            }

                            @Override
                            public boolean resetLimit(String key) {
                                return false;
                            }
                        };
                    }
                };
            }
        }

        public static class TestRateLimitingInjectionResolver extends ParamInjectionResolver<RateLimiting> {
            public TestRateLimitingInjectionResolver() {
                super(TestRateLimitingFactoryProvider.class);
            }
        }

        public static class Binder extends AbstractBinder {

            @Override
            protected void configure() {
                bind(TestRateLimitingFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
                bind(TestRateLimitingFactoryProvider.TestRateLimitingInjectionResolver.class).to(
                        new TypeLiteral<InjectionResolver<RateLimiting>>() {
                        }
                ).in(Singleton.class);
            }
        }
    }
}