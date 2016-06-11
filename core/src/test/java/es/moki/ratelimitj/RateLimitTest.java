package es.moki.ratelimitj;


import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class RateLimitTest {

    @Test
    public void shouldConstruct() throws Exception {

        ImmutableSet<Window> rules = ImmutableSet.of(Window.of(10, TimeUnit.SECONDS, 5), Window.of(3600, TimeUnit.SECONDS, 1000));

        RateLimit rateLimiter = new RateLimit("redis://localhost", rules);

        CompletionStage<Boolean> key = rateLimiter.overLimitSliderWindow("key");

        assertThat(key.toCompletableFuture().get()).isEqualTo(false);

    }
}