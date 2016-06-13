package es.moki.ratelimitj.core;


import rx.Observable;

import java.util.concurrent.CompletionStage;

public class ReactiveRateLimiter {

    Observable<Boolean> overLimitReactive(String key);

    Observable<Boolean> overLimitReactive(String key, int weight);
}
