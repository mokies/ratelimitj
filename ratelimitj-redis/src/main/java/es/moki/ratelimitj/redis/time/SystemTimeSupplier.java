package es.moki.ratelimitj.redis.time;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class SystemTimeSupplier implements TimeSupplier {

    @Override
    public CompletionStage<Long> get() {
        return CompletableFuture.completedFuture(System.currentTimeMillis() / 1000L);
    }
}
