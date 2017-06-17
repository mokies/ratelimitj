package es.moki.ratelimij.dropwizard.filter;


import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ResourceInfo;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IpKeyTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private ResourceInfo resource;

    @Before
    public void beforeEach() throws Exception {
        doReturn(Object.class).when(resource).getResourceClass();
        when(resource.getResourceMethod()).thenReturn(Object.class.getMethod("wait"));
    }

    @DisplayName("IP key should start with 'rlj' prefix")
    @Test
    public void shouldStartWithPrefix() {
        when(request.getRemoteAddr()).thenReturn("293.0.120.7");

        Optional<String> keyName = Key.IP.create(request, resource, null);

        assertThat(keyName.get()).startsWith("rlj:");
    }

    @DisplayName("IP key should include Class and Method names in key")
    @Test
    public void shouldIncludeResourceInKey() {
        when(request.getRemoteAddr()).thenReturn("293.0.120.7");

        Optional<String> keyName = Key.IP.create(request, resource, null);

        assertThat(keyName.get()).contains("java.lang.Object#wait");
    }

    @DisplayName("IP key should include X-Forwarded-For if available")
    @Test
    public void shouldIncludeXForwardedForIfUserNull() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("293.0.113.7,  211.1.16.2");

        Optional<String> keyName = Key.IP.create(request, resource, null);

        assertThat(keyName.get()).contains("xfwd4#293.0.113.7");
    }

    @DisplayName("IP key should include remote IP if available and user and X-Forwarded-For not found")
    @Test
    public void shouldIncludeRemoteIpIfUserAndXForwarded4Null() {
        when(request.getRemoteAddr()).thenReturn("293.0.120.7");

        Optional<String> keyName = Key.IP.create(request, resource, null);

        assertThat(keyName.get()).contains("ip#293.0.120.7");
    }

    @DisplayName("IP key should return absent if no key available")
    @Test
    public void shouldBeAbsent() {
        when(request.getRemoteAddr()).thenReturn(null);

        Optional<String> keyName = Key.IP.create(request, resource, null);

        assertThat(keyName).isNotPresent();
    }

}