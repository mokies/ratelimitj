package es.moki.ratelimitj.core.limiter.request;


import reactor.core.publisher.Mono;

/**
 * An reactive request rate limiter interface.
 */
public interface ReactiveRequestRateLimiter {

    Mono<Boolean> overLimitWhenIncrementedReactive(String key);

    Mono<Boolean> overLimitWhenIncrementedReactive(String key, int weight);

    Mono<Boolean> geLimitWhenIncrementedReactive(String key);

    Mono<Boolean> geLimitWhenIncrementedReactive(String key, int weight);

    Mono<Boolean> resetLimitReactive(String key);
}
