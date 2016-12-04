package com.touch.esp.common.dropwizard.ratelimit.app.api;

import com.touch.esp.common.dropwizard.ratelimit.app.model.LoginRequest;
import com.touch.esp.common.dropwizard.ratelimit.filter.Rate;
import com.touch.esp.common.dropwizard.ratelimit.filter.RateLimited;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.TimeUnit;

import static com.touch.esp.common.dropwizard.ratelimit.filter.Key.DEFAULT;

@Path("/login")
@Consumes(MediaType.APPLICATION_JSON)
public class LoginResource {

    @POST
    @RateLimited(key = DEFAULT, rates = {
        @Rate(duration = 10, timeUnit = TimeUnit.HOURS, limit = 5)
    })
    public Response login(final LoginRequest login) {
        return Response.ok().build();
    }
}
