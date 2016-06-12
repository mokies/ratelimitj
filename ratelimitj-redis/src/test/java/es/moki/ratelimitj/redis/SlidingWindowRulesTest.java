package es.moki.ratelimitj.redis;


import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class SlidingWindowRulesTest {

    @Test
    public void shouldSerialiseToJson() {
        SlidingWindowRules slidingWindowRules = SlidingWindowRules.of(1, TimeUnit.SECONDS, 5);

        assertThat(slidingWindowRules.toJsonArray().toString()).isEqualTo("[1,5]");
    }

    @Test
     public void shouldSerialiseToJsonWithPrecision() {
        SlidingWindowRules slidingWindowRules = SlidingWindowRules.of(1, TimeUnit.SECONDS, 5).withPrecision(10);

        assertThat(slidingWindowRules.toJsonArray().toString()).isEqualTo("[1,5,10]");
     }

    @Test
     public void shouldSerialiseToJsonWithDurationMinutes() {
        SlidingWindowRules slidingWindowRules = SlidingWindowRules.of(1, TimeUnit.MINUTES, 5).withPrecision(10);

        assertThat(slidingWindowRules.toJsonArray().toString()).isEqualTo("[60,5,10]");
     }

}