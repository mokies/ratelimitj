package es.moki.ratelimij.dropwizard.component.app.api;

import es.moki.ratelimij.dropwizard.annotation.Rate;
import es.moki.ratelimij.dropwizard.annotation.RateLimited;
import es.moki.ratelimij.dropwizard.filter.KeyPart;
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.PrincipalImpl;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.TimeUnit;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
public class TrekResource {

    @GET
    @Path("vulcans")
    @RateLimited(groupKeyPrefix = "trek-races", keys = KeyPart.AUTHENTICATED, rates = {@Rate(duration = 10, timeUnit = TimeUnit.HOURS, limit = 10)})
    public Response getVulcans(@Auth PrincipalImpl principle) {
        return Response.ok().build();
    }

    @GET
    @Path("klingons")
    @RateLimited(groupKeyPrefix = "trek-races", keys = KeyPart.AUTHENTICATED, rates = {@Rate(duration = 10, timeUnit = TimeUnit.HOURS, limit = 10)})
    public Response getKlingons(@Auth PrincipalImpl principle) {
        return Response.ok().build();
    }
}
