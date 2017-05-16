package es.moki.ratelimitj.core.limiter.concurrent;


import java.util.concurrent.TimeUnit;

public class ConcurrentLimitRule {

    private int concurrentLimit;
    private long timeoutMillis;
    private String name;

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
     * @param timeOutUnit The time unit.
     * @param timeOut A timeOut for the checkout baton.
     * @return A concurrent limit rule.
     */
    public static ConcurrentLimitRule of(int concurrentLimit, TimeUnit timeOutUnit, long timeOut) {
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
}
