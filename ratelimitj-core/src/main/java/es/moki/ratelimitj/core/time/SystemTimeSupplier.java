package es.moki.ratelimitj.core.time;


import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class SystemTimeSupplier implements TimeSupplier {

    @Override
    public CompletionStage<Long> getAsync() {
        return CompletableFuture.completedFuture(get());
    }

    @Override
    public Mono<Long> getReactive() {
        return Mono.just(get());
    }

    @Override
    public long get() {
        return System.currentTimeMillis() / 1000L;
    }
}
