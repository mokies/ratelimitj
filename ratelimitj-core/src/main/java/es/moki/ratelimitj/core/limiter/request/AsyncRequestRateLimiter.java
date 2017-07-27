package es.moki.ratelimitj.core.limiter.request;


import java.util.concurrent.CompletionStage;

/**
 * An asynchronous rate limiter interface supporting Java 8's {@link java.util.concurrent.CompletionStage}.
 */
@Deprecated
public interface AsyncRequestRateLimiter {

    @Deprecated
    CompletionStage<Boolean> overLimitAsync(String key);

    @Deprecated
    CompletionStage<Boolean> overLimitAsync(String key, int weight);

    @Deprecated
    CompletionStage<Boolean> resetLimitAsync(String key);
}
