RateLimitJ
============

[![Build Status](https://travis-ci.org/mokies/ratelimitj.svg)](https://travis-ci.org/mokies/ratelimitj)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/es.moki.ratelimitj/ratelimitj/badge.svg)](https://maven-badges.herokuapp.com/maven-central/es.moki.ratelimitj/ratelimitj)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

A Java library for rate limiting, assembled using extensible storage and application framework adaptors. The library's interfaces support thread-safe sync, async, and reactive usage patterns.

#### Modules
RateLimitJ is currently under active construction and provides the following plugable modules:

* [Redis sliding window rate limiter](ratelimitj-redis)
* [Hazelcast sliding window rate limiter](ratelimitj-hazelcast)
* [Dropwizard integration](ratelimitj-dropwizard)

#### Features
* Uses an efficient sliding window algorithm for rate limiting
* Multiple limit rules per instance

Binaries/Download
----------------

Binaries and dependency information for Maven, Ivy, Gradle and others can be found at http://search.maven.org.

Example for Maven:

```xml
<dependency>
  <groupId>es.moki</groupId>
  <artifactId>ratelimitj</artifactId>
  <version>ratelimitj-redis</version>
</dependency>
```

#### Prerequisite

* RateLimitJ requires Java 8

#### Roadmap

| Feature       | Status      |
| ------------- |-------------| 
| Redis sliding window rate limiter | beta  |
| Dropwizard integration - bundle | beta |
| Hazelcast sliding window rate limiter | under construction |
| In-memory sliding window rate limiter | under construction |
| Whitelisting & blacklisting of keys | not started |
| Redis fast bucket rate limiter | not started |
| Spring integration | not started |
| In memory fast bucket rate limiter | not started |

#### Credits
This library was inspired by the following articles on sliding window rate limiting with Redis:

* [Introduction to Rate Limiting with Redis Part 1](http://www.dr-josiah.com/2014/11/introduction-to-rate-limiting-with.html)
* [Introduction to Rate Limiting with Redis Part 2](http://www.dr-josiah.com/2014/11/introduction-to-rate-limiting-with_26.html)

#### Authors

* [Craig Baker](https://github.com/mokies)
