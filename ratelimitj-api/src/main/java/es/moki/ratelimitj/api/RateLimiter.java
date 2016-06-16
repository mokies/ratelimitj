package es.moki.ratelimitj.api;

public interface RateLimiter {

    boolean overLimit(String key);

    boolean overLimit(String key, int weight);
}
