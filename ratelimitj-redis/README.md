RateLimitJ - Redis
==================

The RateLimitJ Redis module provides an implementation of a configurable sliding window rate limiting algorithm.
 

Usage
=====

```java
    import com.lambdaworks.redis.RedisClient;
    import es.moki.ratelimitj.core.LimitRule;

    RedisClient client = RedisClient.create("redis://localhost");
    Set<LimitRule> rules = Collections.singleton(LimitRule.of(10, TimeUnit.SECONDS, 5));
    RedisRateLimit rateLimiter = new RedisRateLimit(client, rules);
    
    boolean overLimit = rateLimiter.overLimit("ip:127.0.0.2");
```


Dependancies
============

TODO

Perforamce 
==========

TODO