package es.moki.ratelimitj.dropwizard;


import es.moki.ratelimitj.dropwizard.app.RateLimitApplication;
import es.moki.ratelimitj.dropwizard.app.RateLimitConfiguration;
import es.moki.ratelimitj.dropwizard.app.model.LoginRequest;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class DropwizardRateLimitComponentTest {

    @ClassRule
    public static final DropwizardAppRule<RateLimitConfiguration> RULE =
            new DropwizardAppRule<>(RateLimitApplication.class, ResourceHelpers.resourceFilePath("ratelimit-app.yaml"));

    @Test
    public void loginHandlerRedirectsAfterPost() {
        RestClient client = new RestClient();

        IntStream.rangeClosed(1, 5)
                .forEach(i -> assertThat(client.login().getStatus()).isEqualTo(200));

        assertThat(client.login().getStatus()).isEqualTo(429);
    }

    private static class RestClient {

        Client client = new JerseyClientBuilder().newBuilder().build();

        public Response login() {
            return client.target(
                    String.format("http://localhost:%d/application/login", RULE.getLocalPort()))
                    .request()
                    .post(Entity.json(loginForm()));
        }

        private LoginRequest loginForm() {
            LoginRequest login = new LoginRequest();
            login.username = "heisenberg";
            login.password = "abc123";
            return login;
        }
    }

}
