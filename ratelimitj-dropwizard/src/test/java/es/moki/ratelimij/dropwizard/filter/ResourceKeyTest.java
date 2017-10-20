package es.moki.ratelimij.dropwizard.filter;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.container.ResourceInfo;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResourceKeyTest {
    
    private ResourceInfo resource = mock(ResourceInfo.class);

    @BeforeEach
    void beforeEach() throws Exception {
        doReturn(Object.class).when(resource).getResourceClass();
        when(resource.getResourceMethod()).thenReturn(Object.class.getMethod("wait"));
    }

    @DisplayName("RESOURCE key should start with 'rlj' prefix")
    @Test
    void shouldStartWithPrefix() {

        Optional<String> keyName = Key.RESOURCE.create(null, resource, null);

        assertThat(keyName.get()).startsWith("rlj:");
    }

    @DisplayName("RESOURCE key should include Class and Method names in key")
    @Test
    void shouldIncludeResourceInKey() {
        Optional<String> keyName = Key.RESOURCE.create(null, resource, null);

        assertThat(keyName.get()).contains("java.lang.Object#wait");
    }


}