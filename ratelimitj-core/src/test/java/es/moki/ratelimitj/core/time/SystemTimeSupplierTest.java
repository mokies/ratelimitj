package es.moki.ratelimitj.core.time;


import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SystemTimeSupplierTest {

    @Test
    void shouldGetSystemCurrentTime() {
        Long time = new SystemTimeSupplier().get();
        assertThat(time).isCloseTo(System.currentTimeMillis() / 1000L, Offset.offset(2L));
    }

    @Test
    void shouldGetAsyncSystemCurrentTime() throws Exception {
        Long time = new SystemTimeSupplier().getAsync().toCompletableFuture().get();
        assertThat(time).isCloseTo(System.currentTimeMillis() / 1000L, Offset.offset(2L));
    }

    @Test
    void shouldGetReactiveSystemCurrentTime() throws Exception {
        Long time = new SystemTimeSupplier().getReactive().block();
        assertThat(time).isCloseTo(System.currentTimeMillis() / 1000L, Offset.offset(2L));
    }
}