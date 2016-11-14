package es.moki.ratelimitj.dropwizard.app;


import es.moki.ratelimij.dropwizard.filter.RateLimited;
import es.moki.ratelimitj.dropwizard.app.model.LoginRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/login")
@Consumes(MediaType.APPLICATION_JSON)
public class LoginResource {

    @RateLimited(duration = 10, limit = 5)
    @POST
    public Response login(LoginRequest login) {

        return Response.ok().build();
    }
}
