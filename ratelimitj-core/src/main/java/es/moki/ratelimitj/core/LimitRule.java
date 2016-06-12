package es.moki.ratelimitj.core;

import java.util.OptionalInt;
import java.util.concurrent.TimeUnit;

public class LimitRule {

    private final int durationSeconds;
    private final long limit;
    private final OptionalInt precision;

    private LimitRule(int durationSeconds, long limit) {
        this.durationSeconds = durationSeconds;
        this.limit = limit;
        this.precision = OptionalInt.empty();
    }

    private LimitRule(LimitRule limitRule, int precision) {
        this.durationSeconds = limitRule.durationSeconds;
        this.limit = limitRule.limit;
        this.precision = OptionalInt.of(precision);
    }

    public static LimitRule of(int duration, TimeUnit timeUnit, long limit) {
        return new LimitRule((int) timeUnit.toSeconds(duration), limit);
    }

    public LimitRule withPrecision(int precision) {
        return new LimitRule(this, precision);
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public OptionalInt getPrecision() {
        return precision;
    }

    public long getLimit() {
        return limit;
    }

    // TODO requires equals & hashcode for Set
}
