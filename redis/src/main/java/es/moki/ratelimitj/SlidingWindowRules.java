package es.moki.ratelimitj;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;

import java.util.concurrent.TimeUnit;

public class SlidingWindowRules {

    private final long durationSeconds;
    private final long limit;
    private final int precision;

    private SlidingWindowRules(long durationSeconds, long limit) {
        this.durationSeconds = durationSeconds;
        this.limit = limit;
        this.precision = -1;
    }

    private SlidingWindowRules(SlidingWindowRules slidingWindowRules, int precision) {
        this.durationSeconds = slidingWindowRules.durationSeconds;
        this.limit = slidingWindowRules.limit;
        this.precision = precision;
    }

    public static SlidingWindowRules of(int duration, TimeUnit timeUnit, long limit) {
        return new SlidingWindowRules(timeUnit.toSeconds(duration), limit);
    }

    public SlidingWindowRules withPrecision(int precision) {
        return new SlidingWindowRules(this, precision);
    }

    protected JsonArray toJsonArray() {
        JsonArray array = Json.array().asArray().add(durationSeconds).add(limit);
        if (precision != -1) {
            array.add(precision);
        }
        return array;
    }

}
