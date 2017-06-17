package es.moki.ratelimij.dropwizard.filter;


import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.SecurityContext;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticatedKeyTest {
    
    @Mock
    private ResourceInfo resource;

    @Mock
    private SecurityContext securityContext;

    @Before
    public void beforeEach() throws Exception {
        doReturn(Object.class).when(resource).getResourceClass();
        when(resource.getResourceMethod()).thenReturn(Object.class.getMethod("wait"));
    }

    @DisplayName("AUTHENTICATED key should start with 'rlj' prefix")
    @Test
    public void shouldStartWithPrefix() {
        when(securityContext.getUserPrincipal()).thenReturn(() -> "elliot");

        Optional<String> keyName = Key.AUTHENTICATED.create(null, resource, securityContext);

        assertThat(keyName.get()).startsWith("rlj:");
    }

    @DisplayName("AUTHENTICATED key should include user id if available")
    @Test
    public void shouldIncludeUserId() {
        when(securityContext.getUserPrincipal()).thenReturn(() -> "elliot");

        Optional<String> keyName = Key.AUTHENTICATED.create(null, resource, securityContext);

        assertThat(keyName.get()).contains("usr#elliot");
    }

    @DisplayName("AUTHENTICATED key should return absent if no key available")
    @Test
    public void shouldBeAbsent() {
        Optional<String> keyName = Key.AUTHENTICATED.create(null, resource, securityContext);

        assertThat(keyName).isNotPresent();
    }

}