package es.moki.ratelimij.dropwizard.filter;


import es.moki.ratelimitj.core.api.RateLimiter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Optional;

import static java.util.Objects.nonNull;


public class SimpleRateLimitFilter implements ContainerRequestFilter {

    private final RateLimiter rateLimit;

    @Context
    private HttpServletRequest request;

    public SimpleRateLimitFilter(RateLimiter rateLimit) {
        this.rateLimit = rateLimit;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (hasExceededRateLimit()) {
            requestContext.abortWith(Response.status(429).build());
        }
    }

    private boolean hasExceededRateLimit() {
        return requestKey().map(rateLimit::overLimit).orElse(false);
    }

    protected Optional<String> requestKey() {
        if (nonNull(request.getRemoteUser())) {
            return Optional.of("dwf:user:" + request.getRemoteUser());
        } else if (nonNull(request.getRemoteAddr())) {
            return Optional.of("dwf:ip:" + request.getRemoteAddr());
        }
        return Optional.empty();
    }
}
