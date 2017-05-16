package es.moki.ratelimitj.core.limiter.request;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class RequestLimitRuleTest {

    @Test
    public void shouldHaveDuration1Seconds() {
        RequestLimitRule requestLimitRule = RequestLimitRule.of(1, TimeUnit.SECONDS, 5);

        assertThat(requestLimitRule.getDurationSeconds()).isEqualTo(1);
    }

    @Test
    public void shouldHaveDuration60Seconds() {
        RequestLimitRule requestLimitRule = RequestLimitRule.of(1, TimeUnit.MINUTES, 5);

        assertThat(requestLimitRule.getDurationSeconds()).isEqualTo(60);
    }

    @Test
    public void shouldHaveLimit5() {
        RequestLimitRule requestLimitRule = RequestLimitRule.of(1, TimeUnit.MINUTES, 5);

        assertThat(requestLimitRule.getLimit()).isEqualTo(5);
    }

    @Test
    public void shouldHavePrecisionOf10() {
        RequestLimitRule requestLimitRule = RequestLimitRule.of(1, TimeUnit.SECONDS, 5).withPrecision(10);

        assertThat(requestLimitRule.getPrecision()).isPresent().hasValue(10);
    }

    @Test
    public void shouldHaveNameOfBoom() {
        RequestLimitRule requestLimitRule = RequestLimitRule.of(1, TimeUnit.SECONDS, 5).withName("boom");

        assertThat(requestLimitRule.getName()).isEqualTo("boom");
    }

}