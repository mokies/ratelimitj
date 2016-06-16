package es.moki.ratelimitj.api;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class LimitRulesTest {

    @Test
    public void shouldHaveDuration1Seconds() {
        LimitRule limitRule = LimitRule.of(1, TimeUnit.SECONDS, 5);

        assertThat(limitRule.getDurationSeconds()).isEqualTo(1);
    }

    @Test
     public void shouldHaveDuration60Seconds() {
        LimitRule limitRule = LimitRule.of(1, TimeUnit.MINUTES, 5);

        assertThat(limitRule.getDurationSeconds()).isEqualTo(60);
     }

    @Test
     public void shouldHaveLimit5() {
        LimitRule limitRule = LimitRule.of(1, TimeUnit.MINUTES, 5);

        assertThat(limitRule.getLimit()).isEqualTo(5);
     }

    @Test
     public void shouldHavePrecisionOf10() {
        LimitRule limitRule = LimitRule.of(1, TimeUnit.SECONDS, 5).withPrecision(10);

        assertThat(limitRule.getPrecision().getAsInt()).isEqualTo(10);
     }

}