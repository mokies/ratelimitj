package com.touch.esp.common.dropwizard.ratelimit;

import com.touch.esp.common.dropwizard.ratelimit.app.RateLimitApplication;
import com.touch.esp.common.dropwizard.ratelimit.app.config.RateLimitApplicationConfiguration;
import com.touch.esp.common.dropwizard.ratelimit.app.model.LoginRequest;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class DropwizardRateLimitComponentTest {

    @ClassRule
    public static final DropwizardAppRule<RateLimitApplicationConfiguration> RULE =
            new DropwizardAppRule<>(RateLimitApplication.class, ResourceHelpers.resourceFilePath("ratelimit-app.yml"));

//    @Rule
//    public RedisRule redisRule = new RedisRule(newRemoteRedisConfiguration()
//                                                           .host(RULE.getConfiguration().getRateLimitConfiguration().getHost())
//                                                           .port(RULE.getConfiguration().getRateLimitConfiguration().getPort()).build());
//
//    @Before
//    public void setUp() {
//        redisRule.getDatabaseOperation().deleteAll();
//    }

    @Test
    public void loginHandlerRedirectsAfterPost() {
        final RestClient client = new RestClient();

        IntStream.rangeClosed(1, 2)
                 .forEach(i -> assertThat(client.get().getStatus()).isEqualTo(200));

        IntStream.rangeClosed(1, 5)
                 .forEach(i -> assertThat(client.login().getStatus()).isEqualTo(200));

        assertThat(client.login().getStatus()).isEqualTo(429);

        IntStream.rangeClosed(1, 3)
                 .forEach(i -> assertThat(client.get().getStatus()).isEqualTo(200));

        assertThat(client.get().getStatus()).isEqualTo(429);

    }

    private static class RestClient {

        private final Client client = ClientBuilder.newBuilder().build();

        public Response login() {
            return client.target(
                    String.format("http://localhost:%d/application/login", RULE.getLocalPort()))
                         .request()
                         .post(Entity.json(loginForm()));
        }

        public Response get() {
            return client.target(
                    String.format("http://localhost:%d/application/user/{id}", RULE.getLocalPort()))
                         .resolveTemplate("id", 1)
                         .request()
                         .get();
        }

        private LoginRequest loginForm() {
            return new LoginRequest("heisenberg", "abc123");
        }
    }

}