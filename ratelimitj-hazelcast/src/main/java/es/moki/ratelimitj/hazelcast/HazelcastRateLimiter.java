package es.moki.ratelimitj.hazelcast;

import es.moki.ratelimitj.core.RateLimiter;

public class HazelcastRateLimiter implements RateLimiter {

    public boolean overLimit(String key) {
        return false;
    }

    public boolean overLimit(String key, int weight) {
        return false;
    }
}
