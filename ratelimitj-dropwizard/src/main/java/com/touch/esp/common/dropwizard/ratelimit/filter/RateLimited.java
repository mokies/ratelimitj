package com.touch.esp.common.dropwizard.ratelimit.filter;

import javax.ws.rs.NameBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD})
public @interface RateLimited {

    Key key();

    Rate[] rates();

}