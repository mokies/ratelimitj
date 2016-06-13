package es.moki.ratelimitj.core;

public interface RateLimiter extends AutoCloseable {

    boolean overLimit(String key);

    boolean overLimit(String key, int weight);
}
