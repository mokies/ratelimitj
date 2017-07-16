RateLimitJ - Inmemory
==================

The RateLimitJ Inmemory module provides an implementation of a configurable sliding window rate limiting algorithm.

The Inmemory Module supports (RequestRateLimiter)[], (ConcurrentRequestLimiter)[] interfaces.
 

### Setup

```xml
<dependency>
  <groupId>es.moki.ratelimitj</groupId>
  <artifactId>ratelimitj-inmemory</artifactId>
  <version>${ratelimitj-inmemory.version}</version>
</dependency>
```
 
### Usage

#### Basic Synchronous Example
```java
    import es.moki.ratelimitj.inmemory.request.InMemorySlidingWindowRequestRateLimiter;

    Set<RequestLimitRule> rules = Collections.singleton(RequestLimitRule.of(1, TimeUnit.MINUTES, 50)); // 50 request per minute, per key
    RequestRateLimiter requestRateLimiter = new InMemorySlidingWindowRequestRateLimiter(rules);
    
    boolean overLimit = requestRateLimiter.overLimitWhenIncremented("ip:127.0.0.2");
```

### Dependencies

* Java 8

### Performance 

