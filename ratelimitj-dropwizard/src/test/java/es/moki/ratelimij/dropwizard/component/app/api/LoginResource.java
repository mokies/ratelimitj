package es.moki.ratelimij.dropwizard.component.app.api;

import es.moki.ratelimij.dropwizard.component.app.model.LoginRequest;
import es.moki.ratelimij.dropwizard.annotation.Rate;
import es.moki.ratelimij.dropwizard.annotation.RateLimited;
import es.moki.ratelimij.dropwizard.filter.KeyPart;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.TimeUnit;

@Path("/login")
@Consumes(MediaType.APPLICATION_JSON)
public class LoginResource {

    @POST
    @RateLimited(keys = { KeyPart.ANY }, rates = {
            @Rate(duration = 10, timeUnit = TimeUnit.HOURS, limit = 5)})
    public Response login(final LoginRequest login) {
        return Response.ok().build();
    }
}
