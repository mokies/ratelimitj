package es.moki.ratelimij.dropwizard.component.app.auth;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.PrincipalImpl;

import java.util.Optional;

public class TestOAuthAuthenticator implements Authenticator<String, PrincipalImpl> {

    @Override
    public Optional<PrincipalImpl> authenticate(String credentials) throws AuthenticationException {
        if ("secret".equals(credentials)) {
            return Optional.of(new PrincipalImpl("elliot"));
        }
        return Optional.empty();
    }
}
