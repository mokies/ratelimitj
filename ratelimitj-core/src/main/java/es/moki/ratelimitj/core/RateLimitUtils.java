package es.moki.ratelimitj.core;


import javax.annotation.Nullable;

public class RateLimitUtils {

    private RateLimitUtils() {
        // util class
    }

    public static <T> T coalesce(@Nullable T first, T second) {
        return first == null ? second : first;
    }
}
