RateLimitJ
============

[![Build Status](https://travis-ci.org/mokies/ratelimitj.svg)](https://travis-ci.org/mokies/ratelimitj)

A Java library for rate limiting using extensible storage and application framework adaptors.

#### Modules
RateLimitJ is currently under active construction and provides the following plugable modules:

* [Redis sliding window rate limiter](ratelimitj-redis)
* [Hazelcast sliding window rate limiter](ratelimitj-hazelcast)
* [Dropwizard integration](ratelimitj-dropwizard)

#### Features
* Uses an efficient sliding window algorithm for rate limiting
* Multiple limit rules per instance

#### Prerequisite

* RateLimitJ requires Java 8

#### Roadmap

| Feature       | Status      |
| ------------- |-------------| 
| Redis sliding window rate limiter | beta  |
| Dropwizard integration - bundle | beta |
| Hazelcast sliding window rate limiter | alpha |
| In memory sliding window rate limiter | under construction |
| Whitelisting & blacklisting of keys | not started |
| Redis fast bucket rate limiter | not started |
| Spring integration | not started |
| In memory fast bucket rate limiter | not started |

#### Credis
This library was inspired by the following articles on sliding window rate limiting with Redis:

* [Introduction to Rate Limiting with Redis Part 1](http://www.dr-josiah.com/2014/11/introduction-to-rate-limiting-with.html)
* [Introduction to Rate Limiting with Redis Part 2](http://www.dr-josiah.com/2014/11/introduction-to-rate-limiting-with_26.html)

#### Dependancies
* RateLimitJ requires Java 8

#### Authors

* [Craig Baker](https://github.com/mokies)
