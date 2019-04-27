package es.moki.ratelimitj.core.time;

import reactor.core.publisher.Mono;

import java.util.concurrent.CompletionStage;


public interface TimeSupplier {

    @Deprecated
    CompletionStage<Long> getAsync();

    Mono<Long> getReactive();

    /**
     * Get unix time in seconds
     * @return unix time seconds
     */
    long get();
}
