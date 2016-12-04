package com.touch.esp.common.dropwizard.ratelimit.app.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.touch.esp.common.dropwizard.ratelimit.config.RateLimitBundleConfiguration;
import com.touch.esp.common.dropwizard.ratelimit.config.RateLimitConfiguration;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class RateLimitApplicationConfiguration extends Configuration implements RateLimitBundleConfiguration {

    @Valid
    @NotNull
    @JsonProperty("rateLimit")
    private RateLimitConfiguration rateLimit;

    @Override
    public RateLimitConfiguration getRateLimitConfiguration() {
        return rateLimit;
    }
}
