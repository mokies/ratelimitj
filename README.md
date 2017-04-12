RateLimitJ
============

[![Build Status](https://travis-ci.org/mokies/ratelimitj.svg)](https://travis-ci.org/mokies/ratelimitj)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/es.moki.ratelimitj/ratelimitj-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/es.moki.ratelimitj/ratelimitj-core)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

A Java library for rate limiting, assembled using extensible storage and application framework adaptors. The library's interfaces support thread-safe sync, async, and reactive usage patterns.

#### Modules
RateLimitJ is currently provides the following plugable modules:

* [Redis sliding window rate limiter](ratelimitj-redis)
* [Dropwizard integration](ratelimitj-dropwizard)
* [Hazelcast sliding window rate limiter](ratelimitj-hazelcast) (in development)


#### Features
* Uses an efficient token bucket algorithm for rate limiting
* Multiple limit rules per instance

Binaries/Download
----------------

Binaries and dependency information for Maven, Ivy, Gradle and others can be found at [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22es.moki.ratelimitj%22).

Example for Maven:

```xml
<dependency>
  <groupId>es.moki.ratelimitj</groupId>
  <artifactId>ratelimitj-redis</artifactId>
  <version>${ratelimitj-redis.version}</version>
</dependency>
```

#### Prerequisite

* RateLimitJ requires Java 8

#### Roadmap

| Feature       | Status      |
| ------------- |-------------| 
| Redis sliding window rate limiter | Stable |
| Dropwizard integration - Bundle | Stable |
| Hazelcast sliding window rate limiter | Development |
| In-memory sliding window rate limiter | Development |
| Better metrics logging | not started |
| Rate limiting Toggles (dark launch) for framework integration | not started |
| Whitelisting & blacklisting of keys | not started |
| Rate limit HTTP header responses | not started |
| Spring integration | not started |

#### Credits
This library was inspired by the following articles on sliding window rate limiting with Redis:

* [Introduction to Rate Limiting with Redis Part 1](http://www.dr-josiah.com/2014/11/introduction-to-rate-limiting-with.html)
* [Introduction to Rate Limiting with Redis Part 2](http://www.dr-josiah.com/2014/11/introduction-to-rate-limiting-with_26.html)

#### Background Reading

* [Stripe Blog - Scaling your API with rate limiters](https://stripe.com/blog/rate-limiters)
* [An alternative approach to rate limiting](https://medium.com/figma-design/an-alternative-approach-to-rate-limiting-f8a06cf7c94c)

#### Authors

* [Craig Baker](https://github.com/mokies)
