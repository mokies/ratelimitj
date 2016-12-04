package es.moki.ratelimij.dropwizard.filter;

import javax.ws.rs.NameBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD})
public @interface Rate {

    int duration();

    TimeUnit timeUnit();

    int limit();

}
