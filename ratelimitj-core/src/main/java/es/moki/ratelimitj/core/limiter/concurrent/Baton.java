package es.moki.ratelimitj.core.limiter.concurrent;


public interface Baton extends AutoCloseable {

    void close();

    void pass();
}
