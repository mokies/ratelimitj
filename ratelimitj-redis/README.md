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
    import com.lambdaworks.redis.RedisClient;
    import es.moki.ratelimitj.core.LimitRule;

    RedisClient client = RedisClient.create("redis://localhost");
    Set<LimitRule> rules = Collections.singleton(LimitRule.of(1, TimeUnit.MINUTES, 50)); // 50 request per minute, per key
    RedisRateLimit rateLimiter = new RedisRateLimit(client, rules);
    
    boolean overLimit = rateLimiter.overLimit("ip:127.0.0.2");
```

#### Multi Rule Reactive Example
```java
    import com.lambdaworks.redis.RedisClient;
    import es.moki.ratelimitj.core.LimitRule;

    RedisClient client = RedisClient.create("redis://localhost");

    Set<LimitRule> rules = new HashSet<>();
    rules.add(LimitRule.of(1, TimeUnit.SECONDS, 10));
    rules.add(LimitRule.of(3600, TimeUnit.SECONDS, 240).withPrecision(60));

    RedisRateLimit rateLimiter = new RedisRateLimit(client, rules);
    
    Mono<Boolean> observable = rateLimiter.overLimitReactive("ip:127.0.1.6");
```

### Dependencies

* Java 8
* Redis

### Performance 

