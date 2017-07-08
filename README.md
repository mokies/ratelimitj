RateLimitJ
============

[![Build Status](https://travis-ci.org/mokies/ratelimitj.svg)](https://travis-ci.org/mokies/ratelimitj)
[![Codecov](https://codecov.io/github/mokies/ratelimitj/coverage.svg?branch=master)](https://codecov.io/github/mokies/ratelimitj?branch=master)
[![Maven Central](https://img.shields.io/maven-central/v/es.moki.ratelimitj/ratelimitj-core.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/es.moki.ratelimitj/ratelimitj-core/)
[![license](https://img.shields.io/github/license/mokies/ratelimitj.svg?style=flat-square)](https://github.com/mokies/ratelimitj/blob/master/LICENSE)


A Java library for rate limiting, assembled using extensible storage and application framework adaptors. The library's interfaces support thread-safe sync, async, and reactive usage patterns.

#### Modules
RateLimitJ provides the following stable plugable modules:

* [Redis sliding window rate limiter](ratelimitj-redis)
* [Dropwizard integration](ratelimitj-dropwizard)


#### Features
* Uses an efficient approximated sliding window algorithm for rate limiting
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
| Redis sliding window rate limiter | production |
| Dropwizard integration - Bundle | stable |
| Hazelcast sliding window rate limiter | development |
| In-memory sliding window rate limiter | beta |
| Better metrics logging | not started |
| Rate limiting toggles (dark launch) for framework integration | beta |
| Whitelisting & blacklisting of keys | back log |
| Rate limit HTTP header responses | back log |
| Spring integration | back log |

Building
-----------

RateLimitJ is built with Gradle and requires a local Redis for the Redis module. 
Running `docker-compose up -d` will start Redis.

To build:

```
$ git clone https://github.com/mokies/ratelimij.git
$ cd ratelimij/
$ docker-compose up -d
$ ./gradlew
```


#### Credits
This library was inspired by the following articles on sliding window rate limiting with Redis:

* [Introduction to Rate Limiting with Redis Part 1](http://www.dr-josiah.com/2014/11/introduction-to-rate-limiting-with.html)
* [Introduction to Rate Limiting with Redis Part 2](http://www.dr-josiah.com/2014/11/introduction-to-rate-limiting-with_26.html)

#### Background Reading

* [Stripe Blog - Scaling your API with rate limiters](https://stripe.com/blog/rate-limiters)
* [An alternative approach to rate limiting](https://medium.com/figma-design/an-alternative-approach-to-rate-limiting-f8a06cf7c94c)

#### Authors

* [Craig Baker](https://github.com/mokies)
