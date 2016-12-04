package com.touch.esp.common.dropwizard.ratelimit.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import es.moki.ratelimitj.core.api.LimitRule;
import io.dropwizard.util.Duration;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RateLimitConfiguration {

    private final String host;
    private final Integer port;
    private final Map<String, RateLimit> limits;

    public RateLimitConfiguration(@JsonProperty("host") final String host,
                                  @JsonProperty("port") final Integer port,
                                  @JsonProperty("limits") final Map<String, RateLimit> limits) {
        this.host = host;
        this.port = port;
        this.limits = limits;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public Map<String, RateLimit> getLimits() {
        return limits;
    }

    public static class RateLimit {

        @NotNull
        public final Duration duration;

        @NotNull
        public final Long limit;

        public RateLimit(@JsonProperty("duration") final Duration duration,
                         @JsonProperty("limit") final Long limit) {
            this.duration = duration;
            this.limit = limit;
        }

        public LimitRule toLimitRule() {
            return LimitRule.of((int) duration.toSeconds(), TimeUnit.SECONDS, limit);
        }
    }

}
