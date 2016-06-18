package es.moki.ratelimitj.redis;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import es.moki.ratelimitj.api.LimitRule;
import es.moki.ratelimitj.core.time.time.TimeBanditSupplier;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@Deprecated
@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class RedisSlidingWindowRateLimiterTest {

    private static final Logger LOG = LoggerFactory.getLogger(RedisSlidingWindowRateLimiterTest.class);

    private static RedisClient client;
    private static StatefulRedisConnection<String, String> connect;

    private TimeBanditSupplier timeBandit = new TimeBanditSupplier();

    @BeforeClass
    public static void before() {
        client = RedisClient.create("redis://localhost");
        connect = client.connect();
    }

    @AfterClass
    public static void after() {
        try (StatefulRedisConnection<String, String> connection = client.connect()) {
            connection.sync().flushdb();
        }
        connect.close();
        client.shutdown();
    }

    @Test
    public void shouldLimitSingleWindowAsync() throws Exception {

        ImmutableSet<LimitRule> rules = ImmutableSet.of(LimitRule.of(10, TimeUnit.SECONDS, 5));

        RedisSlidingWindowRateLimiter rateLimiter = new RedisSlidingWindowRateLimiter(connect, rules, timeBandit);

        List<CompletionStage> stageAsserts = new CopyOnWriteArrayList<>();
        Observable.defer(() -> Observable.just("ip:127.0.0.2"))
                .repeat(5)
                .subscribe((key) -> {
                    timeBandit.addUnixTimeMilliSeconds(1000L);
                    stageAsserts.add(rateLimiter.overLimitAsync(key)
                            .thenAccept(result -> assertThat(result).isFalse()));
                });

        for (CompletionStage stage : stageAsserts) {
            stage.toCompletableFuture().get();
        }

        assertThat(rateLimiter.overLimitAsync("ip:127.0.0.2").toCompletableFuture().get()).isTrue();
    }

    @Test
    public void shouldLimitDualWindowAsync() throws Exception {

        ImmutableSet<LimitRule> rules = ImmutableSet.of(LimitRule.of(2, TimeUnit.SECONDS, 5), LimitRule.of(10, TimeUnit.SECONDS, 20));
        RedisSlidingWindowRateLimiter rateLimiter = new RedisSlidingWindowRateLimiter(connect, rules, timeBandit);

        Queue<CompletionStage> stageAsserts = new ConcurrentLinkedQueue<>();
        Observable.defer(() -> Observable.just("ip:127.0.0.10"))
                .repeat(5)
                .subscribe((key) -> {
                    timeBandit.addUnixTimeMilliSeconds(200L);
                    LOG.debug("incrementing rate limiter");
                    stageAsserts.add(rateLimiter.overLimitAsync(key)
                            .thenAccept(result -> assertThat(result).isFalse()));
                });

        for (CompletionStage stage : stageAsserts) {
            stage.toCompletableFuture().get();
        }

        assertThat(rateLimiter.overLimitAsync("ip:127.0.0.10").toCompletableFuture().get()).isTrue();
        timeBandit.addUnixTimeMilliSeconds(1000L);
        assertThat(rateLimiter.overLimitAsync("ip:127.0.0.10").toCompletableFuture().get()).isFalse();
    }

    @Test
    public void shouldLimitSingleWindowSync() throws Exception {

        ImmutableSet<LimitRule> rules = ImmutableSet.of(LimitRule.of(10, TimeUnit.SECONDS, 5));
        RedisSlidingWindowRateLimiter rateLimiter = new RedisSlidingWindowRateLimiter(connect, rules, timeBandit);

        IntStream.rangeClosed(1, 5).forEach(value -> {
            timeBandit.addUnixTimeMilliSeconds(1000L);
            assertThat(rateLimiter.overLimit("ip:127.0.0.5")).isFalse();
        });

        assertThat(rateLimiter.overLimit("ip:127.0.0.5")).isTrue();
    }

    @Test
    public void shouldPerformFastSingleWindow() throws Exception {

        ImmutableSet<LimitRule> rules = ImmutableSet.of(LimitRule.of(1, TimeUnit.MINUTES, 100));
        RedisSlidingWindowRateLimiter rateLimiter = new RedisSlidingWindowRateLimiter(connect, rules, timeBandit);

        Queue<CompletionStage> stageAsserts = new ConcurrentLinkedQueue<>();
        Stopwatch started = Stopwatch.createStarted();
        for (int i = 1; i < 1_000; i++) {
            timeBandit.addUnixTimeMilliSeconds(1L);
            stageAsserts.add(rateLimiter.overLimitAsync("ip:127.0.0.11"));
        }

        for (CompletionStage stage : stageAsserts) {
            stage.toCompletableFuture().get();
        }

        started.stop();
        System.out.println(started);
    }

}
