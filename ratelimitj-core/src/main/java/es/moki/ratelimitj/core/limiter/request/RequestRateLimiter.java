package es.moki.ratelimitj.core.limiter.request;

/**
 * A synchronous request rate limiter interface.
 */
public interface RequestRateLimiter {

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

    /**
     * Determine if the given key has previously reached the configured rate limit.
     * @param key key.
     * @return {@code true} if the key is at the limit, otherwise {@code false}
     */
    boolean atLimit(String key);

    /**
     * Resets the accumulated rate for the given key.
     * @param key key.
     * @return {@code true} if the key existed, otherwise {@code false} .
     */
    boolean resetLimit(String key);
}
