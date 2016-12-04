package es.moki.ratelimij.dropwizard;

import es.moki.ratelimitj.core.api.LimitRule;
import es.moki.ratelimitj.core.api.RateLimiter;

@Deprecated
public interface RateLimitFactory1 {

    RateLimiter create(LimitRule... limitRule);

}
