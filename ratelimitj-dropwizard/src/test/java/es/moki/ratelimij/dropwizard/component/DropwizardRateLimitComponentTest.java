package es.moki.ratelimij.dropwizard.component;

import es.moki.ratelimij.dropwizard.component.app.RateLimitApplication;
import es.moki.ratelimij.dropwizard.component.app.model.LoginRequest;
import es.moki.ratelimitj.redis.extensions.RedisStandaloneFlushExtension;
import io.dropwizard.Configuration;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.stream.IntStream;

import static org.assertj.core.api.Java6Assertions.assertThat;

@ExtendWith(RedisStandaloneFlushExtension.class)
public class DropwizardRateLimitComponentTest {

    @ClassRule
    public static final DropwizardAppRule<Configuration> RULE =
            new DropwizardAppRule<>(RateLimitApplication.class, ResourceHelpers.resourceFilePath("ratelimit-app.yml"));

    @Test
    public void loginHandlerRedirectsAfterPost() {
        final RestClient client = new RestClient();

        IntStream.rangeClosed(1, 2)
                .forEach(i -> assertThat(client.getLimitedByDefault().getStatus()).isEqualTo(200));

        IntStream.rangeClosed(1, 5)
                .forEach(i -> assertThat(client.login().getStatus()).isEqualTo(200));

        assertThat(client.login().getStatus()).isEqualTo(429);

        IntStream.rangeClosed(1, 3)
                .forEach(i -> assertThat(client.getLimitedByDefault().getStatus()).isEqualTo(200));

        assertThat(client.getLimitedByDefault().getStatus()).isEqualTo(429);
    }

    @Test
    public void shouldLimitAuthenticatedUser() {
        final RestClient client = new RestClient();

        IntStream.rangeClosed(1, 10)
                .forEach(i -> assertThat(client.getLimitedByAuthenticatedUser().getStatus()).isEqualTo(200));

        assertThat(client.getLimitedByAuthenticatedUser().getStatus()).isEqualTo(429);
    }

    @Test
    public void shouldLimitedGroupedKeyParts() {
        final RestClient client = new RestClient();

        IntStream.rangeClosed(1, 5)
                .forEach(i -> assertThat(client.getVulcans().getStatus()).isEqualTo(200));

        IntStream.rangeClosed(1, 5)
                .forEach(i -> assertThat(client.getKlingons().getStatus()).isEqualTo(200));

        assertThat(client.getVulcans().getStatus()).isEqualTo(429);

        assertThat(client.getKlingons().getStatus()).isEqualTo(429);

    }

    private static class RestClient {

        private final Client client = ClientBuilder.newBuilder().build();

        Response login() {
            return client.target(
                    String.format("http://localhost:%d/application/login", RULE.getLocalPort()))
                    .request()
                    .post(Entity.json(loginForm()));
        }

        Response getLimitedByDefault() {
            return client.target(
                    String.format("http://localhost:%d/application/user/{id}/default", RULE.getLocalPort()))
                    .resolveTemplate("id", 1)
                    .request()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer secret")
                    .get();
        }

        Response getLimitedByAuthenticatedUser() {
            return client.target(
                    String.format("http://localhost:%d/application/user/{id}/authenticated", RULE.getLocalPort()))
                    .resolveTemplate("id", 1)
                    .request()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer secret")
                    .get();
        }

        Response getKlingons() {
             return client.target(
                     String.format("http://localhost:%d/application/klingons", RULE.getLocalPort()))
                     .request()
                     .header(HttpHeaders.AUTHORIZATION, "Bearer secret")
                     .get();
         }

        Response getVulcans() {
             return client.target(
                     String.format("http://localhost:%d/application/vulcans", RULE.getLocalPort()))
                     .request()
                     .header(HttpHeaders.AUTHORIZATION, "Bearer secret")
                     .get();
         }

        private LoginRequest loginForm() {
            return new LoginRequest("heisenberg", "abc123");
        }
    }

}