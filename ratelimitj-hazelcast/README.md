RateLimitJ - Hazelcast
======================

The RateLimitJ Hazelcast module provides an implementation of a configurable sliding window rate limiting algorithm.

The Hazelcast Module support (RateLimiter)[] interfaces.
 
Usage
=====

#### Basic Synchronous Example
```java
    import com.hazelcast.core.Hazelcast;
    import es.moki.ratelimitj.core.LimitRule;

    Hazelcast hz = Hazelcast.newHazelcastInstance();
    Set<LimitRule> rules = Collections.singleton(LimitRule.of(1, TimeUnit.MINUTES, 50)); // 50 request per minute, per key
    RateLimiter requestRateLimiter = new HazelcastSlidingWindowRateLimiter(hz, rules);
    
    boolean overLimit = requestRateLimiter.overLimit("ip:127.0.0.2");
```


### Dependencies

* Java 8


### Performance 