package es.moki.ratelimitj.redis;


import com.google.common.collect.ImmutableList;
import es.moki.ratelimitj.core.api.LimitRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

//@RunWith(JUnitPlatform.class)
public class LimitRuleJsonSerialiserTest {

    private final LimitRuleJsonSerialiser serialiser = new LimitRuleJsonSerialiser();

    @Test
    @DisplayName("should Encode limit rule in JSON array")
    public void shouldEncode() {

        ImmutableList<LimitRule> rules = ImmutableList.of(LimitRule.of(10, TimeUnit.SECONDS, 10L), LimitRule.of(1, TimeUnit.MINUTES, 20L));

        assertThat(serialiser.encode(rules)).isEqualTo("[[10,10],[60,20]]");

    }
}