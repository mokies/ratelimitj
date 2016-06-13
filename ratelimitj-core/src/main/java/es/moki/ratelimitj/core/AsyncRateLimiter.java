package es.moki.ratelimitj.core;


import java.util.concurrent.CompletionStage;

public interface AsyncRateLimiter extends AutoCloseable {

    CompletionStage<Boolean> overLimitAsync(String key);

    CompletionStage<Boolean> overLimitAsync(String key, int weight);
}
