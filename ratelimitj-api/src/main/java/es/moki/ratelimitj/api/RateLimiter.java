package es.moki.ratelimitj.api;

/**
 * A synchronous rate limiter interface.
 */
public interface RateLimiter {

    /**
     * Determine if the given key, after incrementing by 1, has exceeded the configured rate limit.
     * @param key key.
     * @return {@code true} if the key is over the limit, otherwise {@code false}
     */
    boolean overLimit(String key);

    /**
     * Determine if the given key, after incrementing by the given weight, has exceeded the configured rate limit.
     * @param key key.
     * @param weight A variable weight.
     * @return {@code true} if the key has exceeded the limit, otherwise {@code false} .
     */
    boolean overLimit(String key, int weight);
}
