package es.moki.ratelimitj.redis.time;


import org.assertj.core.data.Offset;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SystemTimeSupplierTest {

    @Test
    public void shouldGetSystemCurrentTime() throws Exception {
        Long time = new SystemTimeSupplier().get().toCompletableFuture().get();
        assertThat(time).isCloseTo(System.currentTimeMillis()/1000L, Offset.offset(2L));
    }
}