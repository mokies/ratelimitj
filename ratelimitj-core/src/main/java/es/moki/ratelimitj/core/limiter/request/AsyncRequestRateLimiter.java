package es.moki.ratelimitj.core.limiter.request;


import java.util.concurrent.CompletionStage;

/**
 * An asynchronous rate limiter interface supporting Java 8's {@link java.util.concurrent.CompletionStage}.
 */
public interface AsyncRequestRateLimiter {

    CompletionStage<Boolean> overLimitOrIncrementAsync(String key);

    CompletionStage<Boolean> overLimitOrIncrementAsync(String key, int weight);

    CompletionStage<Boolean> resetLimitAsync(String key);
}
