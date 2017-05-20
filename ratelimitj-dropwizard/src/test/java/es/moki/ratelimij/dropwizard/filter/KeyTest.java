package es.moki.ratelimij.dropwizard.filter;


import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.SecurityContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KeyTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private ResourceInfo resource;

    @Mock
    private SecurityContext securityContext;

    @Before
    public void beforeEach() throws Exception {
        doReturn(Object.class).when(resource).getResourceClass();
        when(resource.getResourceMethod()).thenReturn(Object.class.getMethod("wait", null));

    }

    @DisplayName("default key should start with 'rlj' prefix")
    @Test
    public void shouldStartWithPrefix() {
        String keyName = Key.DEFAULT.create(request, resource, securityContext);

        assertThat(keyName).startsWith("rlj:");
    }

    @DisplayName("default key should include Class and Method names in key")
    @Test
    public void shouldIncludeResourceInKey() {
        String keyName = Key.DEFAULT.create(request, resource, securityContext);

        assertThat(keyName).contains("java.lang.Object#wait");
    }

    @DisplayName("default key should include user id if available")
    @Test
    public void shouldIncludeUserId() {
        when(securityContext.getUserPrincipal()).thenReturn(() -> "elliot");

        String keyName = Key.DEFAULT.create(request, resource, securityContext);

        assertThat(keyName).contains("usr#elliot");
    }

    @DisplayName("default key should include X-Forwarded-For if available")
    @Test
    public void shouldIncludeXForwardedForIfUserNull() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("293.0.113.7,  211.1.16.2");

        String keyName = Key.DEFAULT.create(request, resource, securityContext);

        assertThat(keyName).contains("xfwd4#293.0.113.7");
    }

    @DisplayName("default key should include remote IP if available and user and X-Forwarded-For not found")
    @Test
    public void shouldIncludeRemoteIpIfUserAndXForwarded4Null() {
        when(request.getRemoteAddr()).thenReturn("293.0.120.7");

        String keyName = Key.DEFAULT.create(request, resource, securityContext);

        assertThat(keyName).contains("ip#293.0.120.7");
    }

}