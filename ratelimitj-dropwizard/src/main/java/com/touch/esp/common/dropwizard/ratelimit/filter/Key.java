package com.touch.esp.common.dropwizard.ratelimit.filter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ResourceInfo;
import java.util.Optional;

import static java.util.Objects.nonNull;

public enum Key implements KeyProvider {

    DEFAULT {
        @Override
        public String create(final HttpServletRequest request,
                             final ResourceInfo resource) {
            return "dwf:" + requestKey(request).orElse("-") + ":" + resourceKey(resource);
        }
    };

    private static final String HEADER = "X-Forwarded-For";

    private static String resourceKey(final ResourceInfo resource) {
        return resource.getResourceClass().getTypeName() + ":" + resource.getResourceMethod().getName();
    }

    private static Optional<String> requestKey(final HttpServletRequest request) {
        if (nonNull(request.getRemoteUser())) {
            return Optional.of("duser:" + request.getRemoteUser());
        } else if (nonNull(request.getHeader(HEADER))) {
            return Optional.of("x-fwd-for:" + request.getHeader(HEADER));
        } else if (nonNull(request.getRemoteAddr())) {
            return Optional.of("ip:" + request.getRemoteAddr());
        }
        return Optional.empty();
    }
}
