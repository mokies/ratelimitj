package es.moki.ratelimitj.core.time;

import reactor.core.publisher.Mono;

import java.util.concurrent.CompletionStage;


public interface TimeSupplier {

    @Deprecated
    CompletionStage<Long> getAsync();

    Mono<Long> getReactive();

    long get();
}
