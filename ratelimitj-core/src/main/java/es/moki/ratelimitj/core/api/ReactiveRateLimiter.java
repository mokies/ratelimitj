package es.moki.ratelimitj.core.api;


import reactor.core.publisher.Mono;

/**
 * An reactive rate limiter interface.
 */
public interface ReactiveRateLimiter {

    Mono<Boolean> overLimitReactive(String key);

    Mono<Boolean> overLimitReactive(String key, int weight);

    Mono<Boolean> resetLimitReactive(String key);
}
