package es.moki.ratelimij.dropwizard.component;

import es.moki.ratelimij.dropwizard.component.app.RateLimitApplication;
import es.moki.ratelimij.dropwizard.component.app.model.LoginRequest;
import es.moki.ratelimitj.redis.extensions.RedisStandaloneFlushExtension;
import io.dropwizard.Configuration;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({
        DropwizardExtensionsSupport.class,
        RedisStandaloneFlushExtension.class
})
public class DropwizardRateLimitComponentTest {

    private final DropwizardAppExtension<Configuration> app = new DropwizardAppExtension<>(
            RateLimitApplication.class,
            ResourceHelpers.resourceFilePath("ratelimit-app.yml")
    );

    @Test
    @Disabled("Calculating # of calls vs rate limit seems to be bugged for limit < 10")
    public void loginHandlerRedirectsAfterPost() {
        final RestClient client = new RestClient(app.getLocalPort());

        IntStream.rangeClosed(1, 2)
                .forEach(i ->
                        assertThat(client.getLimitedByDefault().getStatus())
                                .describedAs("[getLimitedByDefault] call #%d should not exceed the rate limit", i)
                                .isEqualTo(200)
                );

        IntStream.rangeClosed(1, 5)
                .forEach(i ->
                        assertThat(client.login().getStatus())
                                .describedAs("[login] call #%d should not exceed the rate limit", i)
                                .isEqualTo(200)
                );

        assertThat(client.login().getStatus()).isEqualTo(429);

        IntStream.rangeClosed(1, 3)
                .forEach(i ->
                        assertThat(client.getLimitedByDefault().getStatus())
                                .describedAs("[getLimitedByDefault] call #%d should not exceed the rate limit", i + 2)
                                .isEqualTo(200)
                );

        assertThat(client.getLimitedByDefault().getStatus()).isEqualTo(429);
    }

    @Test
    public void shouldLimitAuthenticatedUser() {
        RestClient client = new RestClient(app.getLocalPort());

        IntStream.rangeClosed(1, 10)
                .forEach(i -> assertThat(client.getLimitedByAuthenticatedUser().getStatus()).isEqualTo(200));

        assertThat(client.getLimitedByAuthenticatedUser().getStatus()).isEqualTo(429);
    }

    @Test
    public void shouldLimitedGroupedKeyParts() {
        final RestClient client = new RestClient(app.getLocalPort());

        IntStream.rangeClosed(1, 5)
                .forEach(i -> assertThat(client.getVulcans().getStatus()).isEqualTo(200));

        IntStream.rangeClosed(1, 5)
                .forEach(i -> assertThat(client.getKlingons().getStatus()).isEqualTo(200));

        assertThat(client.getVulcans().getStatus()).isEqualTo(429);

        assertThat(client.getKlingons().getStatus()).isEqualTo(429);
    }

    private static class RestClient {

        private final int localPort;
        private final Client client = ClientBuilder.newBuilder().build();

        public RestClient(int localPort) {
            this.localPort = localPort;
        }

        Response login() {
            return client.target(String.format("http://localhost:%d/application/login", localPort))
                    .request()
                    .post(Entity.json(loginForm()));
        }

        Response getLimitedByDefault() {
            return client.target(String.format("http://localhost:%d/application/user/{id}/default", localPort))
                    .resolveTemplate("id", 1)
                    .request()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer secret")
                    .get();
        }

        Response getLimitedByAuthenticatedUser() {
            return client.target(String.format("http://localhost:%d/application/user/{id}/authenticated", localPort))
                    .resolveTemplate("id", 1)
                    .request()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer secret")
                    .get();
        }

        Response getKlingons() {
             return client.target(String.format("http://localhost:%d/application/klingons", localPort))
                     .request()
                     .header(HttpHeaders.AUTHORIZATION, "Bearer secret")
                     .get();
         }

        Response getVulcans() {
             return client.target(String.format("http://localhost:%d/application/vulcans", localPort))
                     .request()
                     .header(HttpHeaders.AUTHORIZATION, "Bearer secret")
                     .get();
         }

        private LoginRequest loginForm() {
            return new LoginRequest("heisenberg", "abc123");
        }
    }
}