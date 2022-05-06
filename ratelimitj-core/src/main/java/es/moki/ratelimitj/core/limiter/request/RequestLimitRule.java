package es.moki.ratelimitj.core.limiter.request;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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

    private static void checkDuration(Duration duration) {
        requireNonNull(duration, "duration can not be null");
        if (Duration.ofSeconds(1).compareTo(duration) > 0) {
            throw new IllegalArgumentException("duration must be great than 1 second");
        }
    }

    /**
     * Initialise a request rate limit. Imagine the whole duration window as being one large bucket with a single count.
     *
     * @param duration The time the limit will be applied over. The duration must be greater than 1 second.
     * @param limit    A number representing the maximum operations that can be performed in the given duration.
     * @return A limit rule.
     */
    public static RequestLimitRule of(Duration duration, long limit) {
        checkDuration(duration);
        if (limit < 0) {
            throw new IllegalArgumentException("limit must be greater than zero.");
        }
        int durationSeconds = (int) duration.getSeconds();
        return new RequestLimitRule(durationSeconds, limit, durationSeconds);
    }

    /**
     * Controls (approximate) sliding window precision. A lower duration increases precision and minimises the Thundering herd problem - https://en.wikipedia.org/wiki/Thundering_herd_problem
     *
     * @param precision Defines the time precision that will be used to approximate the sliding window. The precision must be greater than 1 second.
     * @return a limit rule
     */
    public RequestLimitRule withPrecision(Duration precision) {
        checkDuration(precision);
        return new RequestLimitRule(this.durationSeconds, this.limit, (int) precision.getSeconds(), this.name, this.keys);
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
    public RequestLimitRule matchingKeys(String... keys) {
        Set<String> keySet = keys.length > 0 ? new HashSet<>(Arrays.asList(keys)) : null;
        return matchingKeys(keySet);
    }

    /**
     * Applies a key to the rate limit that defines to which keys, the rule applies, null for any unmatched key.
     *
     * @param keys Defines a set of keys to which the rule applies.
     * @return a limit rule
     */
    public RequestLimitRule matchingKeys(Set<String> keys) {
        return new RequestLimitRule(this.durationSeconds, this.limit, this.precision, this.name, keys);
    }

    /**
     * @return The limits duration in seconds.
     */
    public int getDurationSeconds() {
        return durationSeconds;
    }

    /**
     * @return The limits precision in seconds.
     */
    public int getPrecisionSeconds() {
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
    @Nullable
    public Set<String> getKeys() {
        return keys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof RequestLimitRule)) {
            return false;
        }
        RequestLimitRule that = (RequestLimitRule) o;
        return durationSeconds == that.durationSeconds
                && limit == that.limit
                && precision == that.precision
                && Objects.equals(name, that.name)
                && Objects.equals(keys, that.keys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(durationSeconds, limit, precision, name, keys);
    }
}
