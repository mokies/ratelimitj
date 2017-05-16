package es.moki.ratelimitj.core.limiter.request;


import java.util.concurrent.CompletionStage;

/**
 * An asynchronous rate limiter interface supporting Java 8's {@link java.util.concurrent.CompletionStage}.
 */
public interface AsyncRequestRateLimiter {

    CompletionStage<Boolean> overLimitAsync(String key);

    CompletionStage<Boolean> overLimitAsync(String key, int weight);

    CompletionStage<Boolean> resetLimitAsync(String key);
}
