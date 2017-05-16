package es.moki.ratelimitj.core.limiter.concurrent;


import java.util.Optional;

public interface ConcurrentRequestLimiter {

    Optional<? extends AutoCloseable> takeBaton(String key);

    Optional<? extends AutoCloseable> takeBaton(String key, int weight);

}
