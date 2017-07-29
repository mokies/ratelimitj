package es.moki.ratelimitj.inmemory.concurrent;


import es.moki.ratelimitj.core.limiter.concurrent.Baton;
import es.moki.ratelimitj.core.limiter.concurrent.ConcurrentLimitRule;
import es.moki.ratelimitj.core.limiter.concurrent.ConcurrentRequestLimiter;
import net.jodah.expiringmap.ExpiringMap;

import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

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
    public Baton acquire(String key) {
        return acquire(key, 1);
    }

    @Override
    public Baton acquire(String key, final int weight) {
        final Semaphore semaphore = expiringKeyMap.computeIfAbsent(key, s -> new Semaphore(rule.getConcurrentLimit(), false));
        if (semaphore.tryAcquire(weight)) {

            // TODO the semaphore needs to expire the if never closed
            return new InMemoryBaton(semaphore, weight);
        }
        return new InMemoryBaton(weight);
    }

    private static class InMemoryBaton implements Baton {

        private final Semaphore semaphore;
        private final int weight;
        private boolean acquired;

        InMemoryBaton(Semaphore semaphore, int weight) {
            this.semaphore = semaphore;
            this.weight = weight;
            acquired = true;
        }

        InMemoryBaton(int weight) {
            this.semaphore = null;
            this.weight = weight;
        }

        @Override
        public void release() {
            if (semaphore == null) {
                throw new IllegalStateException();
            }
            semaphore.release(weight);
        }

        public boolean hasAcquired() {
            return acquired;
        }

        public <T> Optional<T> get(Supplier<T> action) {
            if (!acquired) {
                return Optional.empty();
            }

            try {
                return Optional.of(action.get());
            } finally {
                release();
                acquired = false;
            }
        }

        @Override
        public void doAction(Runnable action) {
            if (!acquired) {
                return;
            }

            try {
                action.run();
            } finally {
                release();
                acquired = false;
            }
        }
    }

}
