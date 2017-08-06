package es.moki.ratelimitj.test.time;


import es.moki.ratelimitj.core.time.TimeSupplier;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A time supplier suitable for testing.
 */
public class TimeBanditSupplier implements TimeSupplier {

    private final AtomicLong time = new AtomicLong(1000000000000L);

    public void setCurrentUnixTimeSeconds(long timeMilliSeconds) {
        time.set(timeMilliSeconds);
    }

    public long addUnixTimeMilliSeconds(long addMilliSeconds) {
        return time.addAndGet(addMilliSeconds);
    }

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
        return time.get() / 1000;
    }
}
