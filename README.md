RateLimitJ
============

[![Build Status](https://travis-ci.org/mokies/ratelimitj.svg)](https://travis-ci.org/mokies/ratelimitj)

The RateLimitJ project aims to provide a modular rate limiting solution allowing for adaptability between backend storage and application frameworks in the Java ecosystem.

RateLimitJ is currently under active construction and provides the following pluggable modules:

* [Redis sliding window rate limiter](ratelimitj-redis)

Dependancies
============

* RateLimitJ requires Java 8

Feature Roadmap
---------------

| Feature       | Status      |
| ------------- |-------------| 
| Redis sliding window rate limiter | alpha  |
| Dropwizard integration - bundle | alpha |
| Hazelcast sliding window rate limiter | active prototyping |
| In memory sliding window rate limiter | active prototyping |
| Redis fast bucket rate limiter | not started |
| In memory fast bucket rate limiter | not started |
| In memory fast bucket rate limiter | not started |
| Spring integration | not started |



Background
----------
This library was inspired by the following articles on sliding window rate limiting with Redis:

* [Introduction to Rate Limiting with Redis Part 1](http://www.dr-josiah.com/2014/11/introduction-to-rate-limiting-with.html)
* [Introduction to Rate Limiting with Redis Part 2](http://www.dr-josiah.com/2014/11/introduction-to-rate-limiting-with_26.html)

For more information on the `weight` and `precision` options, see the second blog post above.

Authors
-------

* [Craig Baker](https://github.com/mokies)
