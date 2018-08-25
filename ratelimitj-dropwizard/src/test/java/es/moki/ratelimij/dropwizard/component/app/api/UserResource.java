package es.moki.ratelimij.dropwizard.component.app.api;

import es.moki.ratelimij.dropwizard.annotation.Rate;
import es.moki.ratelimij.dropwizard.annotation.RateLimited;
import es.moki.ratelimij.dropwizard.filter.KeyPart;
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.PrincipalImpl;

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
    @Path("/{id}/default")
    @RateLimited(keys = { KeyPart.ANY }, rates = {@Rate(duration = 10, timeUnit = TimeUnit.HOURS, limit = 5)})
    public Response getLimitedByDefault(@PathParam("id") final Integer id) {
        return Response.ok().build();
    }

    @GET
    @Path("/{id}/authenticated")
    @RateLimited(keys = { KeyPart.AUTHENTICATED }, rates = {@Rate(duration = 10, timeUnit = TimeUnit.HOURS, limit = 10)})
    public Response getLimitedByAuthenticatedUser(@Auth PrincipalImpl principle, @PathParam("id") final Integer id) {
        return Response.ok().build();
    }

}
