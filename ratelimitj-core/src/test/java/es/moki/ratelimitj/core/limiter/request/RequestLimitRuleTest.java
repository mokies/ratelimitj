package es.moki.ratelimitj.core.limiter.request;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

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

        assertThat(requestLimitRule.getPrecision()).isEqualTo(60);
    }

    @Test
    void shouldHaveLimit5() {
        RequestLimitRule requestLimitRule = RequestLimitRule.of(Duration.ofMinutes(1), 5);

        assertThat(requestLimitRule.getLimit()).isEqualTo(5);
    }

    @Test
    void shouldHavePrecisionOf10() {
        RequestLimitRule requestLimitRule = RequestLimitRule.of(Duration.ofSeconds(1), 5).withPrecision(10);

        assertThat(requestLimitRule.getPrecision()).isEqualTo(10);
    }

    @Test
    void shouldHaveNameOfBoom() {
        RequestLimitRule requestLimitRule = RequestLimitRule.of(Duration.ofSeconds(1), 5).withName("boom");

        assertThat(requestLimitRule.getName()).isEqualTo("boom");
    }

    @Test
    public void shouldHaveLimitGreaterThanZero() {
        assertThatThrownBy(() -> RequestLimitRule.of(Duration.ofSeconds(1), -1).withName("boom")).isInstanceOf(IllegalArgumentException.class);
    }

}