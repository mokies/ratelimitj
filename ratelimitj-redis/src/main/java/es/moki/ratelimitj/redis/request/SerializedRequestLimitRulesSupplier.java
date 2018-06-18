package es.moki.ratelimitj.redis.request;

import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRulesSupplier;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SerializedRequestLimitRulesSupplier implements RequestLimitRulesSupplier<String> {

    private final LimitRuleJsonSerialiser serialiser = new LimitRuleJsonSerialiser();

    private final Map<String,String> serializedRuleMap;

    private final String serializedDefaultRuleSet;

    SerializedRequestLimitRulesSupplier(Set<RequestLimitRule> rules) {
        this.serializedRuleMap = RequestLimitRulesSupplier.buildRuleMap(rules)
                .entrySet()
                .stream()
                .collect(Collectors.toMap( kv -> kv.getKey(), kv -> serialiser.encode(kv.getValue()) ));
        this.serializedDefaultRuleSet = serialiser.encode(RequestLimitRulesSupplier.buildDefaultRuleSet(rules));
    }

    @Override
    public String getRules(String key) {
        String rules = serializedRuleMap.get(key);
        if (rules != null) {
            return rules;
        }
        return serializedDefaultRuleSet;
    }

}
