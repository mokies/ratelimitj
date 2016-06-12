package es.moki.ratelimitj.core;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class SlidingWindowRulesTest {

    @Test
    public void shouldSerialiseToJson() {
        SlidingWindowRule slidingWindowRule = SlidingWindowRule.of(1, TimeUnit.SECONDS, 5);

        assertThat(slidingWindowRule.toJsonArray().toString()).isEqualTo("[1,5]");
    }

    @Test
     public void shouldSerialiseToJsonWithPrecision() {
        SlidingWindowRule slidingWindowRule = SlidingWindowRule.of(1, TimeUnit.SECONDS, 5).withPrecision(10);

        assertThat(slidingWindowRule.toJsonArray().toString()).isEqualTo("[1,5,10]");
     }

    @Test
     public void shouldSerialiseToJsonWithDurationMinutes() {
        SlidingWindowRule slidingWindowRule = SlidingWindowRule.of(1, TimeUnit.MINUTES, 5).withPrecision(10);

        assertThat(slidingWindowRule.toJsonArray().toString()).isEqualTo("[60,5,10]");
     }

}