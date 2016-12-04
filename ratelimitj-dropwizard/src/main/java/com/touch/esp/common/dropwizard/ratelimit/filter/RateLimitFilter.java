package com.touch.esp.common.dropwizard.ratelimit.filter;

import com.touch.esp.common.dropwizard.ratelimit.RateLimitFactory;
import com.touch.esp.common.dropwizard.ratelimit.RateLimiting;
import es.moki.ratelimitj.core.api.LimitRule;
import es.moki.ratelimitj.core.api.RateLimiter;
import org.glassfish.jersey.server.model.AnnotatedMethod;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;

public class RateLimitFilter implements ContainerRequestFilter {

    private static final Integer STATUS = 429;

    @RateLimiting
    private RateLimitFactory factory;

    @Context
    private HttpServletRequest request;

    @Context
    private ResourceInfo resource;

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {

        final AnnotatedMethod method = new AnnotatedMethod(resource.getResourceMethod());
        final RateLimited rateLimited = method.getAnnotation(RateLimited.class);

        final RateLimiter rateLimit = factory.create(toLimitRules(rateLimited));
        final KeyProvider keyProvider = rateLimited.key();
        if (rateLimit.overLimit(keyProvider.create(request, resource))) {
            requestContext.abortWith(Response.status(STATUS).build());
        }
    }

    private LimitRule[] toLimitRules(RateLimited rateLimited) {
        return Arrays.stream(rateLimited.rates()).map(this::toLimitRule).toArray( LimitRule[]::new );
    }

    private LimitRule toLimitRule(Rate rate) {
        return LimitRule.of(rate.duration(), rate.timeUnit(), rate.limit());
    }
}