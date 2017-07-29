https://www.binpress.com/tutorial/introduction-to-rate-limiting-with-redis-part-2/166
https://github.com/dudleycarr/ratelimit.js
https://github.com/nlap/dropwizard-ratelimit/blob/master/bundle/src/main/java/ca/nlap/dropwizard/ratelimit/RateLimitBundle.java
https://github.com/juju/ratelimit
https://github.com/touhonoob/RateLimit
https://stripe.com/blog/rate-limiters


Enhancements 
- Toggle silent failures
- Implement Concurrent Redis
- Implement Concurrent Dropwizard annotations and bindings
- Implement Reactive Request Inmemory. I don't think this is of value.
- Implement Spring integration
- Improve metrics and instrumentation
- Improve test coverage
- Improve performance testing
- Improve documentation
- Caching/Streaming/Buffering over limit reative wrapper that reduces load on backing implementation when under heavy load.
Refer to Google Site Reliability Engineering book and Cloudflare rate limiting blog post.
- Remove dependency on Guava once upgrade to Lettuce 5.0 complete.

