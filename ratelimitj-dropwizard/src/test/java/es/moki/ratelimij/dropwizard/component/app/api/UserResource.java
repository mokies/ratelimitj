package es.moki.ratelimij.dropwizard.component.app.api;

import es.moki.ratelimij.dropwizard.filter.Key;
import es.moki.ratelimij.dropwizard.annotation.RateLimited;
import es.moki.ratelimij.dropwizard.annotation.Rate;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.TimeUnit;

@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @GET
    @Path("/{id}")
    @RateLimited(key = Key.DEFAULT, rates = {@Rate(duration = 10, timeUnit = TimeUnit.HOURS, limit = 5)})
    public Response get(@PathParam("id") final Integer id) {
        return Response.ok().build();
    }
}
