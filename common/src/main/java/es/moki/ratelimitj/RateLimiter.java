package es.moki.ratelimitj;

public interface RateLimiter {

    boolean overLimit(String key);

    boolean overLimit(String key, int weight);
}
