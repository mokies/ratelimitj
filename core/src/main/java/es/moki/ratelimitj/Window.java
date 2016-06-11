package es.moki.ratelimitj;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import java.util.concurrent.TimeUnit;

public class Window {

    private final long duration;
    private final long limit;
    private final int precision;

    private Window (long duration, long limit) {
        this.duration = duration;
        this.limit = limit;
        this.precision = 1;
    }

    private Window (Window window, int precision) {
        this.duration = window.duration;
        this.limit = window.limit;
        this.precision = precision;
    }

    public static Window of(int duration, TimeUnit timeUnit, long limit) {
        return new Window(timeUnit.toSeconds(duration), limit);
    }

    public Window withPrecision(int precision) {
        return new Window(this, precision);
    }

    protected JsonObject toJsonObject() {
        return Json.object().add("duration", duration).add("limit", limit).add("precision", precision);
    }

}
