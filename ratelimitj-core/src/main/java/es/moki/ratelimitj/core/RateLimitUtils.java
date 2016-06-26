package es.moki.ratelimitj.core;


import javax.annotation.Nullable;

public class RateLimitUtils {

    public static <T> T coalesce(@Nullable T first, T second) {
        return first == null ? second : first;
    }
}
