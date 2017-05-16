package es.moki.ratelimitj.core.limiter.request;

import java.util.OptionalInt;
import java.util.concurrent.TimeUnit;

/**
 * Defines a limit rule that can support regular and token bucket rate limits.
 */
public class RequestLimitRule {

    private final int durationSeconds;
    private final long limit;
    private final OptionalInt precision;
    private final String name;

    private RequestLimitRule(int durationSeconds, long limit) {
        this(durationSeconds, limit, OptionalInt.empty(), null);
    }

    private RequestLimitRule(int durationSeconds, long limit, OptionalInt precision, String name) {
        this.durationSeconds = durationSeconds;
        this.limit = limit;
        this.precision = precision;
        this.name = name;
    }

    /**
     * Initialise a regular rate limit. Imagine the whole duration window as being one large bucket with a single count.
     *
     * @param duration The time the limit will be applied over.
     * @param timeUnit The time unit.
     * @param limit    A number representing the maximum operations that can be performed in the given duration.
     * @return A limit rule.
     */

    public static RequestLimitRule of(int duration, TimeUnit timeUnit, long limit) {
        return new RequestLimitRule((int) timeUnit.toSeconds(duration), limit);
    }

    /**
     * Configures as a sliding window rate limit. Imagine the duration window divided into a number of smaller buckets, each with it's own count.
     * The number of smaller buckets is defined by the precision.
     *
     * @param precision Defines the number of buckets that will be used to approximate the sliding window.
     * @return a limit rule
     */
    public RequestLimitRule withPrecision(int precision) {
        return new RequestLimitRule(this.durationSeconds, this.limit, OptionalInt.of(precision), this.name);
    }

    /**
     * Applies a name to the rate limit that is useful for analysis of limits.
     *
     * @param name Defines a descriptive name for the rule limit.
     * @return a limit rule
     */
    public RequestLimitRule withName(String name) {
        return new RequestLimitRule(this.durationSeconds, this.limit, this.precision, name);
    }

    /**
     * @return The limits duration in seconds.
     */
    public int getDurationSeconds() {
        return durationSeconds;
    }

    /**
     * @return The limits precision.
     */
    public OptionalInt getPrecision() {
        return precision;
    }

    /**
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The limit.
     */
    public long getLimit() {
        return limit;
    }

}
