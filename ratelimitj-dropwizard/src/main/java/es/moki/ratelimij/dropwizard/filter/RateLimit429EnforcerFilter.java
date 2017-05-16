package es.moki.ratelimij.dropwizard.filter;

import es.moki.ratelimij.dropwizard.RateLimiting;
import es.moki.ratelimij.dropwizard.annotation.Rate;
import es.moki.ratelimij.dropwizard.annotation.RateLimited;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiterFactory;
import org.glassfish.jersey.server.model.AnnotatedMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class RateLimit429EnforcerFilter implements ContainerRequestFilter {

    private static final int HTTP_STATUS_TOO_MANY_REQUESTS = 429;

    private static final Logger LOG = LoggerFactory.getLogger(RateLimit429EnforcerFilter.class);

    @RateLimiting
    private RequestRateLimiterFactory factory;

    @Context
    private HttpServletRequest request;

    @Context
    private ResourceInfo resource;

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {

        AnnotatedMethod method = new AnnotatedMethod(resource.getResourceMethod());
        RateLimited rateLimited = method.getAnnotation(RateLimited.class);

        RequestRateLimiter rateLimit = factory.getInstance(toLimitRules(rateLimited));
        KeyProvider keyProvider = rateLimited.key();

        String key = keyProvider.create(request, resource);
        boolean overLimit = rateLimit.overLimit(key);
        if (overLimit) {
            if (!rateLimited.reportOnly()) {
                LOG.info("rate-limit key '{}' over limit. HTTP Status 429 returned.", key);
                requestContext.abortWith(Response.status(HTTP_STATUS_TOO_MANY_REQUESTS).build());
            } else {
                LOG.info("rate-limit key '{}' over limit. ReportOnly is true, no action taken.", key);
            }
            LOG.debug("rate-limit key '{}' under limit.", key);
        }
    }

    private Set<RequestLimitRule> toLimitRules(RateLimited rateLimited) {
        return Arrays.stream(rateLimited.rates()).map(this::toLimitRule).collect(Collectors.toSet());
    }

    private RequestLimitRule toLimitRule(Rate rate) {
        return RequestLimitRule.of(rate.duration(), rate.timeUnit(), rate.limit());
    }
}