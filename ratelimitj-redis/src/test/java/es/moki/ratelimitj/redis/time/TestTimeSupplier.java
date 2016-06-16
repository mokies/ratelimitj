package es.moki.ratelimitj.redis.time;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicLong;

public class TestTimeSupplier implements TimeSupplier {

    private AtomicLong time = new AtomicLong(1000000000000L);

    public void setCurrentUnixTimeSeconds(long timeMilliSeconds) {
        time.set(timeMilliSeconds);
    }

    public long addUnixTimeMilliSeconds(long addMilliSeconds) {
        return time.addAndGet(addMilliSeconds);
    }

    @Override
    public CompletionStage<Long> get() {
        return CompletableFuture.completedFuture(time.get()/1000);
    }
}
