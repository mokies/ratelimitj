package es.moki.ratelimij.dropwizard.filter;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;

@ParametersAreNonnullByDefault
public enum KeyPart implements KeyProvider {

    /**
     * The 'any' key will be the first of:
     * <ul>
     * <li>authenticated principle (Dropwizard auth)</li>
     * <li>X-Forwarded-For Header IP address</li>
     * <li>servlet remote address IP</li>
     * </ul>
     */
    ANY {
        @Override
        public Optional<CharSequence> create(final HttpServletRequest request,
                                             final ResourceInfo resource,
                                             final SecurityContext securityContext) {
            return selectOptional(
                    () -> userRequestKey(securityContext),
                    () -> xForwardedForRequestKey(request),
                    () -> ipRequestKey(request));
        }
    },

    /**
     * The 'authenticated' key will be the authenticated principle (Dropwizard auth).
     */
    AUTHENTICATED {
        @Override
        public Optional<CharSequence> create(final HttpServletRequest request,
                                             final ResourceInfo resource,
                                             final SecurityContext securityContext) {
            return userRequestKey(securityContext);
        }
    },

    /**
     * The 'ip' key will be the IP (X-Forwarded-For Header or servlet remote address).
     */
    IP {
        @Override
        public Optional<CharSequence> create(final HttpServletRequest request,
                                             final ResourceInfo resource,
                                             final SecurityContext securityContext) {
            return selectOptional(
                    () -> xForwardedForRequestKey(request),
                    () -> ipRequestKey(request));
        }

    },

    /**
     * The 'resource' key will be the of the resource name.
     */
    RESOURCE_NAME {
        @Override
        public Optional<CharSequence> create(final HttpServletRequest request,
                                             final ResourceInfo resource,
                                             final SecurityContext securityContext) {
            return Optional.of(resource.getResourceClass().getTypeName()
                    + "#" + resource.getResourceMethod().getName());
        }
    };

    static Optional<CharSequence> userRequestKey(SecurityContext securityContext) {
        Principal userPrincipal = securityContext.getUserPrincipal();
        if (isNull(userPrincipal)) {
            return Optional.empty();
        }
        return Optional.of("usr#" + userPrincipal.getName());
    }

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    static Optional<CharSequence> xForwardedForRequestKey(HttpServletRequest request) {

        String header = request.getHeader(X_FORWARDED_FOR);
        if (isNull(header)) {
            return Optional.empty();
        }

        Optional<String> originatingClientIp = Stream.of(header.split(",")).findFirst();
        return originatingClientIp.map(ip -> "xfwd4#" + ip);
    }

    static Optional<CharSequence> ipRequestKey(HttpServletRequest request) {
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

    public static Optional<CharSequence> combineKeysParts(CharSequence groupKeyPrefix, List<KeyProvider> keyParts, HttpServletRequest request, ResourceInfo resource, SecurityContext securityContext) {

        List<CharSequence> keys = Stream.concat(
                Stream.of(groupKeyPrefix),
                keyParts.stream()
                        .map(keyPart -> keyPart.create(request, resource, securityContext))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
        ).collect(Collectors.toList());

        if (keys.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(keys.stream().collect(Collectors.joining(":", "rlj", "")));
    }

}
