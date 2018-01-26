package es.moki.ratelimitj.core.limiter.concurrent;


import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

@ParametersAreNonnullByDefault
public class ConcurrentLimitRule {

    private final int concurrentLimit;
    private final long timeoutMillis;
    private final String name;

    private ConcurrentLimitRule(int concurrentLimit, long timeoutMillis) {
        this(concurrentLimit, timeoutMillis, null);
    }

    private ConcurrentLimitRule(int concurrentLimit, long timeoutMillis, String name) {
        this.concurrentLimit = concurrentLimit;
        this.timeoutMillis = timeoutMillis;
        this.name = name;
    }

    /**
     * Initialise a concurrent rate limit.
     *
     * @param concurrentLimit The concurrent limit.
     * @param timeOutUnit     The time unit.
     * @param timeOut         A timeOut for the checkout baton.
     * @return A concurrent limit rule.
     */
    public static ConcurrentLimitRule of(int concurrentLimit, TimeUnit timeOutUnit, long timeOut) {
        requireNonNull(timeOutUnit, "time out unit can not be null");
        return new ConcurrentLimitRule(concurrentLimit, timeOutUnit.toMillis(timeOut));
    }

    /**
     * Applies a name to the rate limit that is useful for metrics.
     *
     * @param name Defines a descriptive name for the rule limit.
     * @return a limit rule
     */
    public ConcurrentLimitRule withName(String name) {
        return new ConcurrentLimitRule(this.concurrentLimit, this.timeoutMillis, name);
    }


    public int getConcurrentLimit() {
        return concurrentLimit;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConcurrentLimitRule that = (ConcurrentLimitRule) o;
        return concurrentLimit == that.concurrentLimit &&
                timeoutMillis == that.timeoutMillis &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(concurrentLimit, timeoutMillis, name);
    }
}
