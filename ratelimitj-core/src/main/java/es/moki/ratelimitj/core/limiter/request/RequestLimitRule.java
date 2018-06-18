package es.moki.ratelimitj.core.limiter.request;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

/**
 * Defines a limit rule that can support regular and token bucket rate limits.
 */
@ParametersAreNonnullByDefault
public class RequestLimitRule {

    private final int durationSeconds;
    private final long limit;
    private final int precision;
    private final String name;
    private final Set<String> keys;

    private RequestLimitRule(int durationSeconds, long limit, int precision) {
        this(durationSeconds, limit, precision, null);
    }

    private RequestLimitRule(int durationSeconds, long limit, int precision, String name) {
        this(durationSeconds, limit, precision, name, null);
    }

    private RequestLimitRule(int durationSeconds, long limit, int precision, String name, Set<String> keys) {
        this.durationSeconds = durationSeconds;
        this.limit = limit;
        this.precision = precision;
        this.name = name;
        this.keys = keys;
    }

    /**
     * Initialise a request rate limit. Imagine the whole duration window as being one large bucket with a single count.
     *
     * @param duration The time the limit will be applied over.
     * @param timeUnit The time unit.
     * @param limit    A number representing the maximum operations that can be performed in the given duration.
     * @return A limit rule.
     */
    public static RequestLimitRule of(int duration, TimeUnit timeUnit, long limit) {
        requireNonNull(timeUnit, "time unit can not be null");
        int durationSeconds = (int) timeUnit.toSeconds(duration);
        return new RequestLimitRule(durationSeconds, limit, durationSeconds);
    }

    /**
     * Configures as a sliding window rate limit. Imagine the duration window divided into a number of smaller buckets, each with it's own count.
     * The number of smaller buckets is defined by the precision.
     *
     * @param precision Defines the number of buckets that will be used to approximate the sliding window.
     * @return a limit rule
     */
    public RequestLimitRule withPrecision(int precision) {
        return new RequestLimitRule(this.durationSeconds, this.limit, precision, this.name, this.keys);
    }

    /**
     * Applies a name to the rate limit that is useful for metrics.
     *
     * @param name Defines a descriptive name for the rule limit.
     * @return a limit rule
     */
    public RequestLimitRule withName(String name) {
        return new RequestLimitRule(this.durationSeconds, this.limit, this.precision, name, this.keys);
    }

    /**
     * Applies a key to the rate limit that defines to which keys, the rule applies, empty for any unmatched key.
     *
     * @param keys Defines a set of keys to which the rule applies.
     * @return a limit rule
     */
    public RequestLimitRule withKeys(String... keys) {
        Set<String> keySet = keys.length > 0 ? new HashSet<>(Arrays.asList(keys)) : null;
        return withKeys(keySet);
    }

    /**
     * Applies a key to the rate limit that defines to which keys, the rule applies, null for any unmatched key.
     *
     * @param keys Defines a set of keys to which the rule applies.
     * @return a limit rule
     */
    public RequestLimitRule withKeys(Set<String> keys) {
        return new RequestLimitRule(this.durationSeconds, this.limit, this.precision, this.name, keys);
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
    public int getPrecision() {
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

    /**
     * @return The keys.
     */
    public Set<String> getKeys() {
        return keys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RequestLimitRule that = (RequestLimitRule) o;
        return durationSeconds == that.durationSeconds
                && limit == that.limit
                && Objects.equals(precision, that.precision)
                && Objects.equals(name, that.name)
                && Objects.equals(keys, that.keys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(durationSeconds, limit, precision, name, keys);
    }
}
