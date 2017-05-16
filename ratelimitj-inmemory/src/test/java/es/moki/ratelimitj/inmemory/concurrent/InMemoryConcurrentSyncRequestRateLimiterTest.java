package es.moki.ratelimitj.inmemory.concurrent;


import es.moki.ratelimitj.core.limiter.concurrent.Baton;
import es.moki.ratelimitj.core.limiter.concurrent.ConcurrentLimitRule;
import es.moki.ratelimitj.test.limiter.concurrent.AbstractSyncConcurrentRateLimiterTest;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class InMemoryConcurrentSyncRequestRateLimiterTest extends AbstractSyncConcurrentRateLimiterTest {

    @Test
    public void shouldPreventConcurrentRequests() {
        InMemoryConcurrentRequestRateLimiter limiter = new InMemoryConcurrentRequestRateLimiter(
                ConcurrentLimitRule.of(2, TimeUnit.MINUTES, 1));

        assertThat(limiter.takeBaton("key")).isPresent();
        Optional<Baton> baton1 = limiter.takeBaton("key");
        Optional<Baton> baton2 = limiter.takeBaton("key");

        assertThat(baton1).isPresent();
        assertThat(baton2).isNotPresent();
        baton1.get().close();

        Optional<Baton> baton3 = limiter.takeBaton("key");
        assertThat(baton3).isPresent();
    }

    @Test
    public void shouldPreventConcurrentRequestsWithWeight() {
        InMemoryConcurrentRequestRateLimiter limiter = new InMemoryConcurrentRequestRateLimiter(
                ConcurrentLimitRule.of(2, TimeUnit.MINUTES, 1));

        assertThat(limiter.takeBaton("key", 2)).isPresent();
        assertThat(limiter.takeBaton("key")).isNotPresent();
    }

    @Test
    public void shouldTimeOutUnclosedBaton() throws Exception {
        InMemoryConcurrentRequestRateLimiter limiter = new InMemoryConcurrentRequestRateLimiter(
                ConcurrentLimitRule.of(1, TimeUnit.MILLISECONDS, 500));

        assertThat(limiter.takeBaton("key")).isPresent();
        assertThat(limiter.takeBaton("key")).isNotPresent();

        Thread.sleep(1000);

        assertThat(limiter.takeBaton("key")).isPresent();
        assertThat(limiter.takeBaton("key")).isNotPresent();

    }

}