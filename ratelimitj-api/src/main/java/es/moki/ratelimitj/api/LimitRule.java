package es.moki.ratelimitj.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;
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

    private LimitRule(int durationSeconds, long limit, int precision) {
        this.durationSeconds = durationSeconds;
        this.limit = limit;
        this.precision = OptionalInt.of(precision);
    }

    public static Set<LimitRule> acceptableUseRuleSet() {
        return Collections.unmodifiableSet(new HashSet<LimitRule>() {{
            add(new LimitRule(1, 10));
            add(new LimitRule(60, 120));
            add(new LimitRule(3600, 240, 60));
        }});
    }

    public static LimitRule of(int duration, TimeUnit timeUnit, long limit) {
        return new LimitRule((int) timeUnit.toSeconds(duration), limit);
    }

    public LimitRule withPrecision(int precision) {
        return new LimitRule(this.durationSeconds, this.limit, precision);
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
