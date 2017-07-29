package es.moki.ratelimitj.core.limiter.concurrent;


public interface ConcurrentRequestLimiter {

    Baton acquire(String key);

    Baton acquire(String key, int weight);

}
