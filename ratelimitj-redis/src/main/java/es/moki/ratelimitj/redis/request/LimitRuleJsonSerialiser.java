package es.moki.ratelimitj.redis.request;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;

class LimitRuleJsonSerialiser {

    String encode(Iterable<RequestLimitRule> rules) {
        JsonArray jsonArray = Json.array().asArray();
        rules.forEach(rule -> jsonArray.add(toJsonArray(rule)));
        return jsonArray.toString();
    }

    private JsonArray toJsonArray(RequestLimitRule rule) {
        return Json.array().asArray()
                .add(rule.getDurationSeconds())
                .add(rule.getLimit())
                .add(rule.getPrecision());
    }
}
