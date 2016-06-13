package es.moki.ratelimitj.core;


import rx.Observable;

public interface ReactiveRateLimiter {

    Observable<Boolean> overLimitReactive(String key);

    Observable<Boolean> overLimitReactive(String key, int weight);
}
