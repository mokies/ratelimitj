package es.moki.ratelimitj.core.limiter.request;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface RequestLimitRulesSupplier<T> {

    /**
     * Build the default rule set.
     * @param rules
     *      The complete rule set.
     * @return
     *      The rule set for any key that doesn't match a specific rule set.
     */
    static Set<RequestLimitRule> buildDefaultRuleSet(Set<RequestLimitRule> rules) {
        return rules.stream()
                .filter( rule -> rule.getKeys() == null )
                .collect(Collectors.toSet());
    }

    /**
     * Build the rule map.
     * @param rules
     *      The complete rule set.
     * @return
     *      A map of rule set by key.
     */
    static Map<String,Set<RequestLimitRule>> buildRuleMap(Set<RequestLimitRule> rules) {
        Map<String, Set<RequestLimitRule>> ruleMap = new HashMap<>();

        for (RequestLimitRule rule : rules) {
            if (rule.getKeys() == null) {
                continue;
            }
            for (String key : rule.getKeys()) {
                ruleMap.computeIfAbsent(key, k -> new HashSet<>()).add(rule);
            }
        }
        return ruleMap;
    }

    /**
     * Provides the rule set for the given key.
     * @param key
     *      The key for which the rule set should be provided.
     * @return
     *      The rule set for the given key.
     */
    T getRules(String key);

}
