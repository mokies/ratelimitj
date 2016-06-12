package es.moki.ratelimitj;


import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class WindowTest {

    @Test
    public void shouldSerialiseToJson() {
        Window window = Window.of(1, TimeUnit.SECONDS, 5);

        assertThat(window.toJsonArray().toString()).isEqualTo("[1,5]");
    }

    @Test
     public void shouldSerialiseToJsonWithPrecision() {
        Window window = Window.of(1, TimeUnit.SECONDS, 5).withPrecision(10);

        assertThat(window.toJsonArray().toString()).isEqualTo("[1,5,10]");
     }

    @Test
     public void shouldSerialiseToJsonWithDurationMinutes() {
        Window window = Window.of(1, TimeUnit.MINUTES, 5).withPrecision(10);

        assertThat(window.toJsonArray().toString()).isEqualTo("[60,5,10]");
     }

}