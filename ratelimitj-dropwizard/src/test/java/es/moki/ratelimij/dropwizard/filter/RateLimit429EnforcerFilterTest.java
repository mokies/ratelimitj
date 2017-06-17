package es.moki.ratelimij.dropwizard.filter;

import es.moki.ratelimij.dropwizard.RateLimiting;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiterFactory;
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
public class RateLimit429EnforcerFilterTest {

    @Mock
    private static RequestRateLimiterFactory requestRateLimiterFactory;

    @Mock
    private RequestRateLimiter requestRateLimiter;

    @Rule
    public ResourceTestRule rule = ResourceTestRule
            .builder()
            .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
            .addProvider(new TestRateLimitingFactoryProvider.Binder())
            .addProvider(new RateLimited429EnforcerFeature())
            .addResource(new TestResource())
            .build();

    @Test
    @DisplayName("should not limit request")
    public void shouldNotLimit() {

        when(requestRateLimiterFactory.getInstance(anySet())).thenReturn(requestRateLimiter);
        when(requestRateLimiter.overLimitWhenIncremented(anyString())).thenReturn(false);

        Response response = rule.getJerseyTest().target("/test/{id}").resolveTemplate("id", 1)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("should limit request returning a 429")
    public void shouldLimit() {

        when(requestRateLimiterFactory.getInstance(anySet())).thenReturn(requestRateLimiter);
        when(requestRateLimiter.overLimitWhenIncremented(anyString())).thenReturn(true);

        Response response = rule.getJerseyTest().target("/test/{id}").resolveTemplate("id", 1)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();

        assertThat(response.getStatus()).isEqualTo(429);
    }

    @Test
    @DisplayName("should configure rate limiter")
    public void shouldReportOnly() {

        when(requestRateLimiterFactory.getInstance(anySet())).thenReturn(requestRateLimiter);
        when(requestRateLimiter.overLimitWhenIncremented(anyString())).thenReturn(true);


        Response response = rule.getJerseyTest().target("/test/reportOnly/{id}").resolveTemplate("id", 1)
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
                return new AbstractContainerRequestValueFactory<RequestRateLimiterFactory>() {
                    public RequestRateLimiterFactory provide() {
                        return requestRateLimiterFactory;
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