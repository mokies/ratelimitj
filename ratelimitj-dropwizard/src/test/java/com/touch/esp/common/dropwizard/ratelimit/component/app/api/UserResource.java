package com.touch.esp.common.dropwizard.ratelimit.app.api;

import com.touch.esp.common.dropwizard.ratelimit.filter.Rate;
import com.touch.esp.common.dropwizard.ratelimit.filter.RateLimited;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.TimeUnit;

import static com.touch.esp.common.dropwizard.ratelimit.filter.Key.DEFAULT;

@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @GET
    @Path("/{id}")
    @RateLimited(key = DEFAULT, rates = { @Rate(duration = 10, timeUnit = TimeUnit.HOURS, limit = 5) })
    public Response get(@PathParam("id") final Integer id) {
        return Response.ok().build();
    }
}
