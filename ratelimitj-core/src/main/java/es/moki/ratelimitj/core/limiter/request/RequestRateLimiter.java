package es.moki.ratelimitj.core.limiter.request;

/**
 * A synchronous request rate limiter interface.
 */
public interface RequestRateLimiter {

    /**
     * Determine if the given key, after incrementing by one, has exceeded the configured rate limit.
     * @param key key.
     * @return {@code true} if the key is over the limit, otherwise {@code false}
     */
    boolean overLimitWhenIncremented(String key);
   
    /**
     * Regardless of being over limit, we still want to track the count, this is for a client that is going to act
     * regardless of a limit, but, still needs to be counted towareds said limit.
     * @param key
     * @param weight
     * @return {@code true} if the key is over the limit, otherwise {@code false}
     */
	boolean incrementRegardless(String key, int weight);

    /**
     * Determine if the given key, after incrementing by the given weight, has exceeded the configured rate limit.
     * @param key key.
     * @param weight A variable weight.
     * @return {@code true} if the key has exceeded the limit, otherwise {@code false} .
     */
    boolean overLimitWhenIncremented(String key, int weight);

    /**
     * Determine if the given key, after incrementing by one, is &gt;= the configured rate limit.
     * @param key key.
     * @return {@code true} if the key is &gt;== the limit, otherwise {@code false} .
     */
    boolean geLimitWhenIncremented(String key);

    /**
     * Determine if the given key, after incrementing by the given weight, is &gt;= the configured rate limit.
     * @param key key.
     * @param weight A variable weight.
     * @return {@code true} if the key is &gt;== the limit, otherwise {@code false} .
     */
    boolean geLimitWhenIncremented(String key, int weight);

//    /**
//     * Determine if the given key has exceeded the configured rate limit.
//     * @param key key.
//     * @return {@code true} if the key is over the limit, otherwise {@code false}
//     */
//    boolean isOverLimit(String key);
//
//    /**
//     * Determine if the given key is &gt;= the configured rate limit.
//     * @param key key.
//     * @return {@code true} if the key is &gt;== the limit, otherwise {@code false} .
//     */
//    boolean isGeLimit(String key);

    /**
     * Resets the accumulated rate for the given key.
     * @param key key.
     * @return {@code true} if the key existed, otherwise {@code false} .
     */
    boolean resetLimit(String key);

}
