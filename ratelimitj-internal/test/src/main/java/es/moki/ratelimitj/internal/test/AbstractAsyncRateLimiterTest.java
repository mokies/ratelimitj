package es.moki.ratelimitj.internal.test;

import com.google.common.collect.ImmutableSet;
import es.moki.ratelimitj.api.AsyncRateLimiter;
import es.moki.ratelimitj.api.LimitRule;
import es.moki.ratelimitj.core.time.time.TimeBanditSupplier;
import es.moki.ratelimitj.core.time.time.TimeSupplier;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public abstract class AbstractAsyncRateLimiterTest {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final TimeBanditSupplier timeBandit = new TimeBanditSupplier();

    protected abstract AsyncRateLimiter getAsyncRateLimiter(Set<LimitRule> rule, TimeSupplier timeSupplier);

    @Test
    public void shouldLimitSingleWindowAsync() throws Exception {

        ImmutableSet<LimitRule> rules = ImmutableSet.of(LimitRule.of(10, TimeUnit.SECONDS, 5));
        AsyncRateLimiter rateLimiter = getAsyncRateLimiter(rules, timeBandit);

        Queue<CompletionStage> stageAsserts = new ConcurrentLinkedQueue<>();

        Stream.generate(() -> "ip:127.0.0.2").limit(5).forEach(key -> {
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
        AsyncRateLimiter rateLimiter = getAsyncRateLimiter(rules, timeBandit);

        Queue<CompletionStage> stageAsserts = new ConcurrentLinkedQueue<>();

        Stream.generate(() -> "ip:127.0.0.10").limit(5).forEach(key -> {
            timeBandit.addUnixTimeMilliSeconds(200L);
            log.debug("incrementing rate limiter");
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

}
