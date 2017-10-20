package es.moki.ratelimitj.test.limiter.request;

import com.google.common.collect.ImmutableSet;
import es.moki.ratelimitj.core.limiter.request.AsyncRequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.test.time.TimeBanditSupplier;
import es.moki.ratelimitj.core.time.TimeSupplier;
import org.junit.jupiter.api.Test;
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
public abstract class AbstractAsyncRequestRateLimiterTest {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final TimeBanditSupplier timeBandit = new TimeBanditSupplier();

    protected abstract AsyncRequestRateLimiter getAsyncRateLimiter(Set<RequestLimitRule> rule, TimeSupplier timeSupplier);

    @Test
    void shouldLimitSingleWindowAsync() throws Exception {

        ImmutableSet<RequestLimitRule> rules = ImmutableSet.of(RequestLimitRule.of(10, TimeUnit.SECONDS, 5));
        AsyncRequestRateLimiter rateLimiter = getAsyncRateLimiter(rules, timeBandit);

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
    void shouldLimitDualWindowAsync() throws Exception {

        ImmutableSet<RequestLimitRule> rules = ImmutableSet.of(RequestLimitRule.of(2, TimeUnit.SECONDS, 5), RequestLimitRule.of(10, TimeUnit.SECONDS, 20));
        AsyncRequestRateLimiter rateLimiter = getAsyncRateLimiter(rules, timeBandit);

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

    @Test
    void shouldResetLimit() throws Exception {
        ImmutableSet<RequestLimitRule> rules = ImmutableSet.of(RequestLimitRule.of(60, TimeUnit.SECONDS, 1));
        AsyncRequestRateLimiter rateLimiter = getAsyncRateLimiter(rules, timeBandit);

        String key =  "ip:127.1.0.1";

        assertThat(rateLimiter.overLimitAsync(key).toCompletableFuture().get()).isFalse();
        assertThat(rateLimiter.overLimitAsync(key).toCompletableFuture().get()).isTrue();

        assertThat(rateLimiter.resetLimitAsync(key).toCompletableFuture().get()).isTrue();
        assertThat(rateLimiter.resetLimitAsync(key).toCompletableFuture().get()).isFalse();

        assertThat(rateLimiter.overLimitAsync(key).toCompletableFuture().get()).isFalse();
    }

}
