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

class ResourceKeyPartTest {
    
    private ResourceInfo resource = mock(ResourceInfo.class);

    @BeforeEach
    void beforeEach() throws Exception {
        doReturn(Object.class).when(resource).getResourceClass();
        when(resource.getResourceMethod()).thenReturn(Object.class.getMethod("wait"));
    }

    @DisplayName("RESOURCE key should include Class and Method names in key")
    @Test
    void shouldIncludeResourceInKey() {
        Optional<CharSequence> keyName = KeyPart.RESOURCE_NAME.create(null, resource, null);

        assertThat(keyName.get()).isEqualTo("java.lang.Object#wait");
    }

}