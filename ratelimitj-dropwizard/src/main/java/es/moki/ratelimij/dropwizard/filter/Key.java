package es.moki.ratelimij.dropwizard.filter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.isNull;

public enum Key implements KeyProvider {

    /**
     * The default key will use the first of authenticated Principle (dropwizard auth), X-Forwarded-For eader
     * or HTTPServlet Remote Address IP as the rate limit key.
     */
    DEFAULT {
        @Override
        public String create(final HttpServletRequest request,
                             final ResourceInfo resource,
                             final SecurityContext securityContext) {
            return "rlj:" + resourceKey(resource) + ":" + requestKey(request, securityContext);
        }

        private String requestKey(final HttpServletRequest request, final SecurityContext securityContext) {
            return selectOptional(
                    () -> userRequestKey(securityContext),
                    () -> xForwardedForRequestKey(request),
                    () -> ipRequestKey(request))
                    .orElse("NO_REQUEST_KEY");
        }
    };

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private static String resourceKey(final ResourceInfo resource) {
        return resource.getResourceClass().getTypeName()
                + "#" + resource.getResourceMethod().getName();
    }

    static Optional<String> userRequestKey(SecurityContext securityContext) {
        Principal userPrincipal = securityContext.getUserPrincipal();
        if (isNull(userPrincipal)) {
            return Optional.empty();
        }
        return Optional.of("usr#" + userPrincipal.getName());
    }

    static Optional<String> xForwardedForRequestKey(HttpServletRequest request) {

        String header = request.getHeader(X_FORWARDED_FOR);
        if (isNull(header)) {
            return Optional.empty();
        }

        Optional<String> originatingClientIp = Stream.of(header.split(",")).findFirst();
        return originatingClientIp.map(ip -> "xfwd4#" + ip);
    }

    static Optional<String> ipRequestKey(HttpServletRequest request) {
        String remoteAddress = request.getRemoteAddr();
        if (isNull(remoteAddress)) {
            return Optional.empty();
        }
        return Optional.of("ip#" + remoteAddress);
    }

    @SafeVarargs
    static <T> Optional<T> selectOptional(Supplier<Optional<T>>... optionals) {
        return Arrays.stream(optionals)
                .reduce((s1, s2) -> () -> s1.get().map(Optional::of).orElseGet(s2))
                .orElse(Optional::empty).get();
    }
}
