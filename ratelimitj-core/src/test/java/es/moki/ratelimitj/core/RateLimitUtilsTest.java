package es.moki.ratelimitj.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitUtilsTest {

    @Test
    void shouldReturnFirst() {
        Object one = new Object();
        Object two = new Object();

        assertThat(RateLimitUtils.coalesce(one, two)).isEqualTo(one);
    }

    @Test
    void shouldReturnSecond() {
        Object one = null;
        Object two = new Object();

        assertThat(RateLimitUtils.coalesce(one, two)).isEqualTo(two);
    }
}