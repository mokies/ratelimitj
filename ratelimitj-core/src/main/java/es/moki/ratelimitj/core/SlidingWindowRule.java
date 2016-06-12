package es.moki.ratelimitj.core;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;

import java.util.OptionalInt;
import java.util.concurrent.TimeUnit;

public class SlidingWindowRule {

    private final int durationSeconds;
    private final long limit;
    private final OptionalInt precision;

    private SlidingWindowRule(int durationSeconds, long limit) {
        this.durationSeconds = durationSeconds;
        this.limit = limit;
        this.precision = OptionalInt.empty();
    }

    private SlidingWindowRule(SlidingWindowRule slidingWindowRule, int precision) {
        this.durationSeconds = slidingWindowRule.durationSeconds;
        this.limit = slidingWindowRule.limit;
        this.precision = OptionalInt.of(precision);
    }

    public static SlidingWindowRule of(int duration, TimeUnit timeUnit, long limit) {
        return new SlidingWindowRule((int) timeUnit.toSeconds(duration), limit);
    }

    public SlidingWindowRule withPrecision(int precision) {
        return new SlidingWindowRule(this, precision);
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public OptionalInt getPrecision() {
        return precision;
    }

    // TODO requires equals & hashcode for Set

    public JsonArray toJsonArray() {
        JsonArray array = Json.array().asArray().add(durationSeconds).add(limit);
        if (precision.isPresent()) {
            array.add(precision.getAsInt());
        }
        return array;
    }


}
