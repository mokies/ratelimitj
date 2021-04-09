package es.moki.ratelimitj.redis.request;


import com.google.common.collect.ImmutableList;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class RequestLimitRuleJsonSerialiserTest {

    private final LimitRuleJsonSerialiser serialiser = new LimitRuleJsonSerialiser();

    @Test
    @DisplayName("should encode limit rule in JSON array")
    void shouldEncode() {

        ImmutableList<RequestLimitRule> rules = ImmutableList.of(RequestLimitRule.of(Duration.ofSeconds(10), 10L),
                RequestLimitRule.of(Duration.ofMinutes(1), 20L));

        assertThat(serialiser.encode(rules)).isEqualTo("[[10,10,10,0],[60,20,60,0]]");
    }

    @Test
    @DisplayName("should encode limit rule with precision in JSON array")
    void shouldEncodeWithPrecisions() {

        ImmutableList<RequestLimitRule> rules = ImmutableList.of(RequestLimitRule.of(Duration.ofSeconds(10), 10L).withPrecision(Duration.ofSeconds(4)),
                RequestLimitRule.of(Duration.ofMinutes(1), 20L).withPrecision(Duration.ofSeconds(8)));

        assertThat(serialiser.encode(rules)).isEqualTo("[[10,10,4,0],[60,20,8,0]]");
    }

    @Test
    @DisplayName("should encode limit rule with backoff in JSON array")
    void shouldEncodeWithBackoff() {

        ImmutableList<RequestLimitRule> rules = ImmutableList.of(
                RequestLimitRule.of(Duration.ofSeconds(10), 10L).withBackoff(Duration.ofSeconds(15)),
                RequestLimitRule.of(Duration.ofMinutes(1), 20L).withBackoff(Duration.ofMinutes(2))
        );

        assertThat(serialiser.encode(rules)).isEqualTo("[[10,10,10,15],[60,20,60,120]]");
    }
}