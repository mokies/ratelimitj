package es.moki.ratelimitj.inmemory.concurrent;


import es.moki.ratelimitj.core.limiter.concurrent.Baton;
import es.moki.ratelimitj.core.limiter.concurrent.ConcurrentLimitRule;
import es.moki.ratelimitj.core.limiter.concurrent.ConcurrentRequestLimiter;
import net.jodah.expiringmap.ExpiringMap;

import java.util.Optional;
import java.util.concurrent.Semaphore;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static net.jodah.expiringmap.ExpirationPolicy.ACCESSED;

public class InMemoryConcurrentRequestRateLimiter implements ConcurrentRequestLimiter {

    private final ExpiringMap<String, Semaphore> expiringKeyMap;
    private final ConcurrentLimitRule rule;

    public InMemoryConcurrentRequestRateLimiter(ConcurrentLimitRule rule) {
        this.rule = rule;
        expiringKeyMap = ExpiringMap.builder().expiration(rule.getTimeoutMillis(), MILLISECONDS).expirationPolicy(ACCESSED).build();
    }

    @Override
    public Optional<Baton> takeBaton(String key) {
        return takeBaton(key, 1);
    }

    @Override
    public Optional<Baton> takeBaton(String key, final int weight) {
        final Semaphore semaphore = expiringKeyMap.computeIfAbsent(key, s -> new Semaphore(rule.getConcurrentLimit(), false));
        if (semaphore.tryAcquire(weight)) {

            // TODO the semaphore needs to expire the if never closed
            return Optional.of(new InMemoryBaton(semaphore, weight));
        }
        return Optional.empty();
    }

    private static class InMemoryBaton implements Baton {
        private final Semaphore semaphore;
        private final int weight;

        InMemoryBaton(Semaphore semaphore, int weight) {
            this.semaphore = semaphore;
            this.weight = weight;
        }

        @Override
        public void close() {
            pass();
        }

        @Override
        public void pass() {
            semaphore.release(weight);
        }
    }

}
