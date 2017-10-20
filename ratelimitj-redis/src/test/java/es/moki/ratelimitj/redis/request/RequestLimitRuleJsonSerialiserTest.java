package es.moki.ratelimitj.redis.request;


import com.google.common.collect.ImmutableList;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class RequestLimitRuleJsonSerialiserTest {

    private final LimitRuleJsonSerialiser serialiser = new LimitRuleJsonSerialiser();

    @Test
    @DisplayName("should encode limit rule in JSON array")
    void shouldEncode() {

        ImmutableList<RequestLimitRule> rules = ImmutableList.of(RequestLimitRule.of(10, TimeUnit.SECONDS, 10L), RequestLimitRule.of(1, TimeUnit.MINUTES, 20L));

        assertThat(serialiser.encode(rules)).isEqualTo("[[10,10],[60,20]]");
    }

    @Test
    @DisplayName("should encode limit rule with precision in JSON array")
    void shouldEncodeWithPrecisions() {

        ImmutableList<RequestLimitRule> rules = ImmutableList.of(RequestLimitRule.of(10, TimeUnit.SECONDS, 10L).withPrecision(4), RequestLimitRule.of(1, TimeUnit.MINUTES, 20L).withPrecision(8));

        assertThat(serialiser.encode(rules)).isEqualTo("[[10,10,4],[60,20,8]]");
    }

}