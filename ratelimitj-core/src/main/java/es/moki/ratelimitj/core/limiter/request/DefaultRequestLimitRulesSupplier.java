package es.moki.ratelimitj.core.limiter.request;

import java.util.Map;
import java.util.Set;

public class DefaultRequestLimitRulesSupplier implements RequestLimitRulesSupplier<Set<RequestLimitRule>> {

    private final Set<RequestLimitRule> defaultRules;

    private final Map<String, Set<RequestLimitRule>> ruleMap;

    public DefaultRequestLimitRulesSupplier(Set<RequestLimitRule> rules) {
        this.defaultRules = RequestLimitRulesSupplier.buildDefaultRuleSet(rules);
        this.ruleMap = RequestLimitRulesSupplier.buildRuleMap(rules);
    }

    @Override
    public Set<RequestLimitRule> getRules(String key) {
        Set<RequestLimitRule> ruleSet = ruleMap.get(key);
        if (ruleSet != null) {
            return ruleSet;
        }
        return defaultRules;
    }

}
