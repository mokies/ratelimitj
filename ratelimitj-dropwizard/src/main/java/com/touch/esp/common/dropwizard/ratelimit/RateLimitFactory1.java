package com.touch.esp.common.dropwizard.ratelimit;

import es.moki.ratelimitj.core.api.LimitRule;
import es.moki.ratelimitj.core.api.RateLimiter;

public interface RateLimitFactory {

    RateLimiter create(LimitRule... limitRule);

}
