package es.moki.ratelimitj.inmemory.concurrent;


import es.moki.ratelimitj.core.limiter.concurrent.Baton;
import es.moki.ratelimitj.core.limiter.concurrent.ConcurrentLimitRule;
import es.moki.ratelimitj.test.limiter.concurrent.AbstractSyncConcurrentRateLimiterTest;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class InMemoryConcurrentSyncRequestRateLimiterTest extends AbstractSyncConcurrentRateLimiterTest {

    @Test
    public void shouldPreventConcurrentRequests() {
        InMemoryConcurrentRequestRateLimiter limiter = new InMemoryConcurrentRequestRateLimiter(
                ConcurrentLimitRule.of(2, TimeUnit.MINUTES, 1));

        assertThat(limiter.acquire("key").hasAcquired()).isTrue();
        Baton baton1 = limiter.acquire("key");
        Baton baton2 = limiter.acquire("key");

        assertThat(baton1.hasAcquired()).isTrue();
        assertThat(baton2.hasAcquired()).isFalse();
        baton1.release();

        Baton baton3 = limiter.acquire("key");
        assertThat(baton3.hasAcquired()).isTrue();
    }

    @Test
    public void shouldPreventConcurrentRequestsWithWeight() {
        InMemoryConcurrentRequestRateLimiter limiter = new InMemoryConcurrentRequestRateLimiter(
                ConcurrentLimitRule.of(2, TimeUnit.MINUTES, 1));

        assertThat(limiter.acquire("key", 2).hasAcquired()).isTrue();
        assertThat(limiter.acquire("key").hasAcquired()).isFalse();
    }

    @Test
    public void shouldTimeOutUnclosedBaton() throws Exception {
        InMemoryConcurrentRequestRateLimiter limiter = new InMemoryConcurrentRequestRateLimiter(
                ConcurrentLimitRule.of(1, TimeUnit.MILLISECONDS, 500));

        assertThat(limiter.acquire("key").hasAcquired()).isTrue();
        assertThat(limiter.acquire("key").hasAcquired()).isFalse();

        Thread.sleep(1000);

        assertThat(limiter.acquire("key").hasAcquired()).isTrue();
        assertThat(limiter.acquire("key").hasAcquired()).isFalse();
    }

    @Test
    public void shouldDoWork() {
        InMemoryConcurrentRequestRateLimiter limiter = new InMemoryConcurrentRequestRateLimiter(
                ConcurrentLimitRule.of(1, TimeUnit.MINUTES, 1));

        Integer result = limiter.acquire("key")
                .get(this::executeSomeMethod)
                .orElseThrow(() -> new RuntimeException("concurrent limit exceeded"));

        assertThat(result).isEqualTo(1);
        
        assertThat(limiter.acquire("key").hasAcquired()).isTrue();
        assertThat(limiter.acquire("key").hasAcquired()).isFalse();


        limiter.acquire("key").doAction(this::executeSomeMethod);
    }


    private Integer executeSomeMethod() {
        return 1;
    }

}