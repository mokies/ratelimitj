RateLimitJ - Redis
==================

The RateLimitJ Redis module provides an implementation of a configurable sliding window rate limiting algorithm.

The Redis Module support (RateLimiter)[], (AsyncRateLimiter)[] and (ReactiveRateLimiter)[] interfaces.
 

### Setup

```xml
<dependency>
  <groupId>es.moki.ratelimitj</groupId>
  <artifactId>ratelimitj-redis</artifactId>
  <version>x.x.x</version>
</dependency>
```
 
### Usage

#### Basic Synchronous Example
```java
        RedisRateLimiterFactory factory = new RedisRateLimiterFactory(RedisClient.create("redis://localhost"));
        Set<RequestLimitRule> rules = Collections.singleton(RequestLimitRule.of(1, TimeUnit.MINUTES, 50)); // 50 request per minute, per key
        RequestRateLimiter requestRateLimiter = factory.getInstance(rules);

        boolean overLimit = requestRateLimiter.overLimitWhenIncremented("ip:127.0.0.2");
```

#### Redis Cluster Example
```java
        RequestRateLimiterFactory factory = new RedisClusterRateLimiterFactory(RedisClusterClient.create("redis://localhost"));
        Set<RequestLimitRule> rules = Collections.singleton(RequestLimitRule.of(1, TimeUnit.MINUTES, 50)); // 50 request per minute, per key
        RequestRateLimiter requestRateLimiter = factory.getInstance(rules);

        boolean overLimit = requestRateLimiter.overLimitWhenIncremented("ip:127.0.0.2");
```

#### Multi Rule Reactive Example
```java
    RedisRateLimiterFactory factory = new RedisRateLimiterFactory(RedisClient.create("redis://localhost"));;

    Set<RequestLimitRule> rules = new HashSet<>();
    rules.add(RequestLimitRule.of(1, TimeUnit.SECONDS, 10));
    rules.add(RequestLimitRule.of(3600, TimeUnit.SECONDS, 240).withPrecision(60));
    
    ReactiveRequestRateLimiter requestRateLimiter = factory.getInstance(rules);
        
    Mono<Boolean> overLimit = requestRateLimiter.overLimitWhenIncrementedReactive("ip:127.0.1.6");
```

### Dependencies

* Java 8
* Redis

### Performance 

