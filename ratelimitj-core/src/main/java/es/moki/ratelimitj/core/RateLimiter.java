package es.moki.ratelimitj.core;

public interface RateLimiter {

    boolean overLimit(String key);

    boolean overLimit(String key, int weight);
}
