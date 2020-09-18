package es.moki.ratelimij.dropwizard.filter;

import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiterFactory;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith({
        DropwizardExtensionsSupport.class,
        MockitoExtension.class
})
public class RateLimit429EnforcerFilterTest {

    @Mock private static RequestRateLimiterFactory requestRateLimiterFactory;
    @Mock private RequestRateLimiter requestRateLimiter;

    private final ResourceExtension resources = ResourceExtension.builder()
            .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
            .addProvider(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(requestRateLimiterFactory).to(RequestRateLimiterFactory.class);
                }
            })
            .addProvider(new RateLimited429EnforcerFeature())
            .addResource(new TestResource())
            .build();

    @Test
    @DisplayName("should not limit request")
    public void shouldNotLimit() {
        when(requestRateLimiterFactory.getInstance(anySet())).thenReturn(requestRateLimiter);
        when(requestRateLimiter.overLimitWhenIncremented(anyString())).thenReturn(false);

        Response response = resources.getJerseyTest().target("/test/{id}").resolveTemplate("id", 1)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("should limit request returning a 429")
    public void shouldLimit() {
        when(requestRateLimiterFactory.getInstance(anySet())).thenReturn(requestRateLimiter);
        when(requestRateLimiter.overLimitWhenIncremented(anyString())).thenReturn(true);

        Response response = resources.getJerseyTest().target("/test/{id}").resolveTemplate("id", 1)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();

        assertThat(response.getStatus()).isEqualTo(429);
    }

    @Test
    @DisplayName("should configure rate limiter")
    public void shouldReportOnly() {
        when(requestRateLimiterFactory.getInstance(anySet())).thenReturn(requestRateLimiter);
        when(requestRateLimiter.overLimitWhenIncremented(anyString())).thenReturn(true);

        Response response = resources.getJerseyTest().target("/test/reportOnly/{id}").resolveTemplate("id", 1)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("should not limit if the backing rate limiter throws exception")
    public void shouldNotLimitIfBackingRateLimiterFails() {
        when(requestRateLimiterFactory.getInstance(anySet())).thenReturn(requestRateLimiter);
        when(requestRateLimiter.overLimitWhenIncremented(anyString())).thenThrow(new RuntimeException());

        Response response = resources.getJerseyTest().target("/test/{id}").resolveTemplate("id", 1)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
    }
}