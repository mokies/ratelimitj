package es.moki.ratelimitj.redis;


import com.google.common.collect.ImmutableList;
import es.moki.ratelimitj.api.LimitRule;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class LimitRuleJsonSerialiserTest {

    private LimitRuleJsonSerialiser serialiser = new LimitRuleJsonSerialiser();

    @Test
    public void shouldEncode() {

        ImmutableList<LimitRule> rules = ImmutableList.of(LimitRule.of(10, TimeUnit.SECONDS, 10L), LimitRule.of(1, TimeUnit.MINUTES, 20L));

        assertThat(serialiser.encode(rules)).isEqualTo("[[10,10],[60,20]]");

    }
}