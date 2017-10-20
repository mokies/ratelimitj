package es.moki.ratelimitj.core.limiter.request;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class RequestLimitRuleTest {

    @Test
    void shouldHaveDuration1Seconds() {
        RequestLimitRule requestLimitRule = RequestLimitRule.of(1, TimeUnit.SECONDS, 5);

        assertThat(requestLimitRule.getDurationSeconds()).isEqualTo(1);
    }

    @Test
    void shouldHaveDuration60Seconds() {
        RequestLimitRule requestLimitRule = RequestLimitRule.of(1, TimeUnit.MINUTES, 5);

        assertThat(requestLimitRule.getDurationSeconds()).isEqualTo(60);
    }

    @Test
    void shouldHaveLimit5() {
        RequestLimitRule requestLimitRule = RequestLimitRule.of(1, TimeUnit.MINUTES, 5);

        assertThat(requestLimitRule.getLimit()).isEqualTo(5);
    }

    @Test
    void shouldHavePrecisionOf10() {
        RequestLimitRule requestLimitRule = RequestLimitRule.of(1, TimeUnit.SECONDS, 5).withPrecision(10);

        assertThat(requestLimitRule.getPrecision()).isPresent().hasValue(10);
    }

    @Test
    void shouldHaveNameOfBoom() {
        RequestLimitRule requestLimitRule = RequestLimitRule.of(1, TimeUnit.SECONDS, 5).withName("boom");

        assertThat(requestLimitRule.getName()).isEqualTo("boom");
    }

}