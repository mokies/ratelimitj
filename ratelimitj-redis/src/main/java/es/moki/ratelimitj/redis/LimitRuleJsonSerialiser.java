package es.moki.ratelimitj.redis;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import es.moki.ratelimitj.api.LimitRule;

import java.util.OptionalInt;

class LimitRuleJsonSerialiser {

    String encode(Iterable<LimitRule> rules) {
        JsonArray jsonArray = Json.array().asArray();
        rules.forEach(rule -> jsonArray.add(toJsonArray(rule)));
        return jsonArray.toString();
    }

    private JsonArray toJsonArray(LimitRule rule) {
        JsonArray array = Json.array().asArray().add(rule.getDurationSeconds()).add(rule.getLimit());

        OptionalInt precision = rule.getPrecision();
        if (precision.isPresent()) {
            array.add(precision.getAsInt());
        }
        return array;
    }
}
