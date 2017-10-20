package es.moki.ratelimitj.core.limiter.concurrent;


import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ConcurrentLimitRuleTest {

    @Test
    void shouldHaveTimeout1Seconds() {
        ConcurrentLimitRule limitRule = ConcurrentLimitRule.of(10, TimeUnit.SECONDS,  1);

        assertThat(limitRule.getTimeoutMillis()).isEqualTo(1000);
    }

    @Test
    void shouldHaveTimeout60Seconds() {
        ConcurrentLimitRule limitRule = ConcurrentLimitRule.of(10, TimeUnit.MINUTES,  1);

        assertThat(limitRule.getTimeoutMillis()).isEqualTo(60000);
    }

    @Test
    void shouldHaveConcurrentLimit5() {
        ConcurrentLimitRule limitRule = ConcurrentLimitRule.of(5, TimeUnit.MINUTES, 5);

        assertThat(limitRule.getConcurrentLimit()).isEqualTo(5);
    }

    @Test
    void shouldHaveNameOfBoom() {
        ConcurrentLimitRule limitRule = ConcurrentLimitRule.of(1, TimeUnit.SECONDS, 5).withName("boom");

        assertThat(limitRule.getName()).isEqualTo("boom");
    }
}


