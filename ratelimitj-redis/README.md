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
    StatefulRedisConnection redisScriptingReactiveCommands = RedisClient.create("redis://localhost").connect();
    
    Set<RequestLimitRule> rules = Collections.singleton(RequestLimitRule.of(1, TimeUnit.MINUTES, 50)); // 50 request per minute, per key
    RequestRateLimiter requestRateLimiter = new RedisSlidingWindowRequestRateLimiter(redisScriptingReactiveCommands, rules);
        
    boolean overLimit = requestRateLimiter.overLimitWhenIncremented("ip:127.0.0.2");
```

#### Multi Rule Reactive Example
```java
    StatefulRedisConnection redisScriptingReactiveCommands = RedisClient.create("redis://localhost").connect();

    Set<RequestLimitRule> rules = new HashSet<>();
    rules.add(RequestLimitRule.of(1, TimeUnit.SECONDS, 10));
    rules.add(RequestLimitRule.of(3600, TimeUnit.SECONDS, 240).withPrecision(60));
    
    ReactiveRequestRateLimiter requestRateLimiter = new RedisSlidingWindowRequestRateLimiter(redisScriptingReactiveCommands, rules);
        
    Mono<Boolean> observable = requestRateLimiter.overLimitWhenIncrementedReactive("ip:127.0.1.6");
```

### Dependencies

* Java 8
* Redis

### Performance 

