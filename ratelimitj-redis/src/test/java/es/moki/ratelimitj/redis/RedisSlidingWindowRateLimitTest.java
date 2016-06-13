package es.moki.ratelimitj.redis;


import com.google.common.collect.ImmutableSet;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import es.moki.ratelimitj.core.LimitRule;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class RedisSlidingWindowRateLimitTest {

    private static final Logger LOG = LoggerFactory.getLogger(RedisSlidingWindowRateLimitTest.class);

    private static RedisClient client;

    @BeforeClass
    public static void before() {
        client = RedisClient.create("redis://localhost");
    }

    @AfterClass
    public static void after() {
        try(StatefulRedisConnection<String, String> connection = client.connect()) {
            connection.sync().flushdb();
        }
        client.shutdown();
    }

    @Test
    public void shouldLimitSingleWindowAsync() throws Exception {

        ImmutableSet<LimitRule> rules = ImmutableSet.of(LimitRule.of(10, TimeUnit.SECONDS, 5));
        RedisSlidingWindowRateLimit rateLimiter = new RedisSlidingWindowRateLimit(client, rules);

        List<CompletionStage> stageAsserts = new CopyOnWriteArrayList<>();
        Observable.defer(() -> Observable.just("ip:127.0.0.2"))
                .repeat(5)
                .subscribe((key) -> {

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
        RedisSlidingWindowRateLimit rateLimiter = new RedisSlidingWindowRateLimit(client, rules);

        List<CompletionStage> stageAsserts = new CopyOnWriteArrayList<>();
        Observable.defer(() -> Observable.just("ip:127.0.0.10"))
                .repeat(5)
                .subscribe((key) -> {
                    LOG.debug("incrementing rate limiter");
                    stageAsserts.add(rateLimiter.overLimitAsync(key)
                            .thenAccept(result -> assertThat(result).isFalse()));
                });

        for (CompletionStage stage : stageAsserts) {
            stage.toCompletableFuture().get();
        }

        assertThat(rateLimiter.overLimitAsync("ip:127.0.0.10").toCompletableFuture().get()).isTrue();
        Thread.sleep(2000);
        assertThat(rateLimiter.overLimitAsync("ip:127.0.0.10").toCompletableFuture().get()).isFalse();
    }

    @Test
    public void shouldLimitSingleWindowSync() throws Exception {

        ImmutableSet<LimitRule> rules = ImmutableSet.of(LimitRule.of(10, TimeUnit.SECONDS, 5));
        RedisSlidingWindowRateLimit rateLimiter = new RedisSlidingWindowRateLimit(client, rules);

        IntStream.rangeClosed(1, 5).forEach(value -> {
            boolean result = rateLimiter.overLimit("ip:127.0.0.5");
            AssertionsForClassTypes.assertThat(result).isFalse();
        });

        assertThat(rateLimiter.overLimit("ip:127.0.0.5")).isTrue();
    }

    @Test
    public void shouldWorkWithRedisTime() throws Exception {

        ImmutableSet<LimitRule> rules = ImmutableSet.of(LimitRule.of(10, TimeUnit.SECONDS, 5), LimitRule.of(3600, TimeUnit.SECONDS, 1000));
        RedisSlidingWindowRateLimit rateLimiter = new RedisSlidingWindowRateLimit(client, rules, true);

        CompletionStage<Boolean> key = rateLimiter.overLimitAsync("ip:127.0.0.3");

        assertThat(key.toCompletableFuture().get()).isFalse();
    }

}
