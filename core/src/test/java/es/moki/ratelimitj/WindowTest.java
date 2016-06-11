package es.moki.ratelimitj;


import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class WindowTest {

    @Test
    public void shouldSerialiseToJson() {
        Window window = Window.of(1, TimeUnit.SECONDS, 5);

        assertThat(window.toJsonObject().toString()).isEqualTo("{\"interval\":1,\"limit\":5,\"precision\":1}");
    }

    @Test
     public void shouldSerialiseToJsonWithPrecision() {
        Window window = Window.of(1, TimeUnit.SECONDS, 5).withPrecision(10);

        assertThat(window.toJsonObject().toString()).isEqualTo("{\"interval\":1,\"limit\":5,\"precision\":10}");
     }

}