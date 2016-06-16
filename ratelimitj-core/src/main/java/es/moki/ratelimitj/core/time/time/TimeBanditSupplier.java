package es.moki.ratelimitj.core.time.time;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicLong;

public class TimeBanditSupplier implements TimeSupplier {

    private AtomicLong time = new AtomicLong(1000000000000L);

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
    public long get() {
        return time.get()/1000;
    }
}
