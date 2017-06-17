package es.moki.ratelimitj.core.limiter.request;


import reactor.core.publisher.Mono;

/**
 * An reactive request rate limiter interface.
 */
public interface ReactiveRequestRateLimiter {

    Mono<Boolean> overLimitOrIncrementReactive(String key);

    Mono<Boolean> overLimitOrIncrementReactive(String key, int weight);

    Mono<Boolean> resetLimitReactive(String key);
}
