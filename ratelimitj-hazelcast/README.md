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
    Set<RequestLimitRule> rules = Collections.singleton(RequestLimitRule.of(Duration.ofMinutes(1), 50)); // 50 request per minute, per key
    RequestRateLimiter requestRateLimiter = new HazelcastSlidingWindowRequestRateLimiter(hz, rules);
    
    boolean overLimit = requestRateLimiter.overLimit("ip:127.0.0.2");
```


### Dependencies

* Java 8


### Performance 