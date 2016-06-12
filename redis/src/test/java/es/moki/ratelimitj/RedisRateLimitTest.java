package es.moki.ratelimitj;


import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import rx.Observable;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class RedisRateLimitTest {

    private static final String KEY =  "127.0.0.1";

    @Test
    public void shouldConnect() throws Exception {

        ImmutableSet<Window> rules = ImmutableSet.of(Window.of(10, TimeUnit.SECONDS, 1));

        try (RedisRateLimit rateLimiter = new RedisRateLimit("redis://localhost", rules)) {

            assertThat(rateLimiter.overLimitAsync("key").toCompletableFuture().get()).isEqualTo(false);
        }
    }

    @Test
    public void shouldLimitSingleWindow() throws Exception {

        ImmutableSet<Window> rules = ImmutableSet.of(Window.of(10, TimeUnit.SECONDS, 5));

        // TODO close connection
        RedisRateLimit rateLimiter = new RedisRateLimit("redis://localhost", rules);

        List<CompletionStage> stageAsserts = newArrayList();
        CountDownLatch latch = new CountDownLatch(5);
        Observable.defer(() -> Observable.just(KEY))
                .repeatWhen(observable -> observable.delay(100, TimeUnit.MILLISECONDS).take(5))
                .repeat(5)
                .subscribe((key) -> {
                    latch.countDown();
                    stageAsserts.add(rateLimiter.overLimitAsync(key)
                            .thenAccept(result -> assertThat(result).isEqualTo(false)));
                });
        latch.await();

        for (CompletionStage stage : stageAsserts) {
            stage.toCompletableFuture().get();
        }

        assertThat(rateLimiter.overLimitAsync(KEY).toCompletableFuture().get()).isEqualTo(true);

    }

    @Test
    public void shouldWorkWithRedisTime() throws Exception {

        ImmutableSet<Window> rules = ImmutableSet.of(Window.of(10, TimeUnit.SECONDS, 5), Window.of(3600, TimeUnit.SECONDS, 1000));

        try (RedisRateLimit rateLimiter = new RedisRateLimit("redis://localhost", rules, true)) {

            CompletionStage<Boolean> key = rateLimiter.overLimitAsync("key");

            assertThat(key.toCompletableFuture().get()).isEqualTo(false);
        }

    }


}