package es.moki.ratelimitj.core.limiter.request;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
class DefaultRequestLimitRulesSupplierTest {

    private final Set<RequestLimitRule> allRules = new HashSet<>();
    private final RequestLimitRulesSupplier<Set<RequestLimitRule>> requestLimitRulesSupplier;

    DefaultRequestLimitRulesSupplierTest() {
        allRules.add(RequestLimitRule.of(Duration.ofSeconds(1), 10).withName("localhostPerSeconds")
                .withKeys("localhost", "127.0.0.1"));
        allRules.add(RequestLimitRule.of(Duration.ofHours(1), 2000).withName("localhostPerHours")
                .withKeys("localhost", "127.0.0.1"));
        allRules.add(RequestLimitRule.of(Duration.ofSeconds(1), 5).withName("perSeconds"));
        allRules.add(RequestLimitRule.of(Duration.ofHours(1), 1000).withName("perHours"));
        requestLimitRulesSupplier = new DefaultRequestLimitRulesSupplier(allRules);
    }

    @Test
    void shouldContainsDefaultRules() {
        Set<String> ruleNames = requestLimitRulesSupplier.getRules("other")
                .stream()
                .map(RequestLimitRule::getName)
                .collect(Collectors.toSet());

        assertThat(ruleNames).hasSize(2);
        assertThat(ruleNames).contains("perSeconds", "perHours");
    }

    @Test
    void shouldContainLocalhostRulesForLocalhost() {
        Set<String> ruleNames = requestLimitRulesSupplier.getRules("localhost")
                .stream()
                .map(RequestLimitRule::getName)
                .collect(Collectors.toSet());

        assertThat(ruleNames).hasSize(2);
        assertThat(ruleNames).contains("localhostPerSeconds", "localhostPerHours");
    }

    @Test
    void shouldContainLocalhostRulesFor127_0_0_1() {
        Set<String> ruleNames = requestLimitRulesSupplier.getRules("127.0.0.1")
                .stream()
                .map(RequestLimitRule::getName)
                .collect(Collectors.toSet());

        assertThat(ruleNames).hasSize(2);
        assertThat(ruleNames).contains("localhostPerSeconds", "localhostPerHours");
    }

}
