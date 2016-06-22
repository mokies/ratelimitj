package es.moki.ratelimitj.api;


import rx.Observable;

/**
 * An reactive rate limiter interface.
 */
public interface ReactiveRateLimiter {

    Observable<Boolean> overLimitReactive(String key);

    Observable<Boolean> overLimitReactive(String key, int weight);
}
