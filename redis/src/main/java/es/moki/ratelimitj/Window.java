package es.moki.ratelimitj;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;

import java.util.concurrent.TimeUnit;

public class Window {

    private final long durationSeconds;
    private final long limit;
    private final int precision;

    private Window (long durationSeconds, long limit) {
        this.durationSeconds = durationSeconds;
        this.limit = limit;
        this.precision = -1;
    }

    private Window (Window window, int precision) {
        this.durationSeconds = window.durationSeconds;
        this.limit = window.limit;
        this.precision = precision;
    }

    public static Window of(int duration, TimeUnit timeUnit, long limit) {
        return new Window(timeUnit.toSeconds(duration), limit);
    }

    public Window withPrecision(int precision) {
        return new Window(this, precision);
    }

    protected JsonArray toJsonArray() {
        JsonArray array = Json.array().asArray().add(durationSeconds).add(limit);
        if (precision != -1) {
            array.add(precision);
        }
        return array;
    }

}
