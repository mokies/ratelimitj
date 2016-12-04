package es.moki.ratelimij.dropwizard.filter;

import es.moki.ratelimij.dropwizard.RateLimiting;
import es.moki.ratelimitj.core.api.RateLimiter;
import es.moki.ratelimitj.core.api.RateLimiterFactory;
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
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class RateLimitFilterTest {

    @Mock
    private static RateLimiterFactory rateLimiterFactory;

    @Mock
    private RateLimiter rateLimiter;

    @Rule
    public ResourceTestRule rule = ResourceTestRule
            .builder()
            .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
            .addProvider(new TestRateLimitingFactoryProvider.Binder())
            .addProvider(new RateLimitFeature())
            .addResource(new TestResource())
            .build();

    @Test
    @DisplayName("should configure rate limiter")
    public void shouldAdd() {

        when(rateLimiterFactory.getInstance(anySet())).thenReturn(rateLimiter);
        when(rateLimiter.overLimit(anyString())).thenReturn(false);

        Response response = rule.getJerseyTest().target("/test/{id}").resolveTemplate("id", 1)
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
                return new AbstractContainerRequestValueFactory<RateLimiterFactory>() {
                    public RateLimiterFactory provide() {
                        return rateLimiterFactory;
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