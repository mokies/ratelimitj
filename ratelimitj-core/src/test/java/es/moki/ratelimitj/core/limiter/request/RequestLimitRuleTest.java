package es.moki.ratelimitj.core.limiter.request;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestLimitRuleTest {

    @Test
    void shouldHaveDuration1Seconds() {

        RequestLimitRule requestLimitRule = RequestLimitRule.of(Duration.ofSeconds(1), 5);

        assertThat(requestLimitRule.getDurationSeconds()).isEqualTo(1);
    }

    @Test
    void shouldHaveDuration60Seconds() {
        RequestLimitRule requestLimitRule = RequestLimitRule.of(Duration.ofMinutes(1), 5);

        assertThat(requestLimitRule.getDurationSeconds()).isEqualTo(60);
    }

    @Test
    void shouldDefaultPrecisionToEqualDuration() {
        RequestLimitRule requestLimitRule = RequestLimitRule.of(Duration.ofMinutes(1), 5);

        assertThat(requestLimitRule.getPrecisionSeconds()).isEqualTo(60);
    }

    @Test
    void shouldHaveLimit5() {
        RequestLimitRule requestLimitRule = RequestLimitRule.of(Duration.ofMinutes(1), 5);

        assertThat(requestLimitRule.getLimit()).isEqualTo(5);
    }

    @Test
    void shouldHavePrecisionOf10() {
        RequestLimitRule requestLimitRule = RequestLimitRule.of(Duration.ofSeconds(1), 5).withPrecision(Duration.ofSeconds(10));

        assertThat(requestLimitRule.getPrecisionSeconds()).isEqualTo(10);
    }

    @Test
    void shouldHaveBackoffOf10() {
        RequestLimitRule requestLimitRule = RequestLimitRule.of(Duration.ofSeconds(1), 5).withBackoff(Duration.ofSeconds(10));

        assertThat(requestLimitRule.getBackoffSeconds()).isEqualTo(10);
    }

    @Test
    void shouldHaveNameOfBoom() {
        RequestLimitRule requestLimitRule = RequestLimitRule.of(Duration.ofSeconds(1), 5).withName("boom");

        assertThat(requestLimitRule.getName()).isEqualTo("boom");
    }

    @Test
    void shouldHaveLimitGreaterThanZero() {
        assertThatThrownBy(() -> RequestLimitRule.of(Duration.ofSeconds(1), -1).withName("boom")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldHaveDurationGreaterThanOneSecond() {
        assertThatThrownBy(() -> RequestLimitRule.of(Duration.ofMillis(100), 1).withName("boom"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("duration must be greater than 1 second");
    }

    @Test
    void shouldHavePrecisionGreaterThanOneSecond() {
        assertThatThrownBy(() -> RequestLimitRule.of(Duration.ofSeconds(20), 1).withPrecision(Duration.ofMillis(100)).withName("boom"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("precision must be greater than 1 second");
    }

    @Test
    void shouldHaveBackoffGreaterThanDuration() {
        assertThatThrownBy(() -> RequestLimitRule.of(Duration.ofSeconds(1), 1).withBackoff(Duration.ofMillis(100)).withName("boom"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("backoff must be greater than 1 second");
    }

}