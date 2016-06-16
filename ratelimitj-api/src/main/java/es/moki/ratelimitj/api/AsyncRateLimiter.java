package es.moki.ratelimitj.api;


import java.util.concurrent.CompletionStage;

public interface AsyncRateLimiter {

    CompletionStage<Boolean> overLimitAsync(String key);

    CompletionStage<Boolean> overLimitAsync(String key, int weight);
}
