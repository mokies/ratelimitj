package es.moki.ratelimij.dropwizard.filter;


import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ResourceInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KeyTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private ResourceInfo resource;

    @Before
    public void beforeEach() throws Exception {
        doReturn(Object.class).when(resource).getResourceClass();
        when(resource.getResourceMethod()).thenReturn(Object.class.getMethod("wait", null)) ;
    }

    @DisplayName("default key should start with 'dw-ratelimit' prefix")
    @Test
    public void shouldStartWithPrefix() {
        String keyName = Key.DEFAULT.create(request, resource);

        assertThat(keyName).startsWith("rlj:");
    }

    @DisplayName("default key should include Class and Method names in key")
    @Test
    public void shouldEndWithResourceInKey() {
        String keyName = Key.DEFAULT.create(request, resource);

        assertThat(keyName).endsWith("java.lang.Object#wait");
    }


    @DisplayName("default key should include user id if available")
    @Test
    public void shouldIncludeUserId() {
        when(request.getRemoteUser()).thenReturn("elliot");

        String keyName = Key.DEFAULT.create(request, resource);

        assertThat(keyName).contains("usr#elliot");
    }

    @DisplayName("default key should include user id if available")
    @Test
    public void shouldIncludeXForwardedForIfUserNull() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.7,  211.1.16.2");

        String keyName = Key.DEFAULT.create(request, resource);

        assertThat(keyName).contains("xfwd4#203.0.113.7");
    }

}