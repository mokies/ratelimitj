package es.moki.ratelimij.dropwizard.filter;

import es.moki.ratelimij.dropwizard.annotation.Rate;
import es.moki.ratelimij.dropwizard.annotation.RateLimited;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.TimeUnit;

@Path("/test")
@Consumes(MediaType.APPLICATION_JSON)
public class TestResource {


    @GET
    @Path("/{id}")
    @RateLimited(keys = { KeyPart.ANY }, rates = {@Rate(duration = 10, timeUnit = TimeUnit.HOURS, limit = 5)})
    public Response get(@PathParam("id") final Integer id) {
        return Response.ok().build();
    }

    @GET
    @Path("/reportOnly/{id}")
    @RateLimited(keys = { KeyPart.ANY }, reportOnly = true, rates = {@Rate(duration = 10, timeUnit = TimeUnit.HOURS, limit = 5)})
    public Response getReportOnly(@PathParam("id") final Integer id) {
        return Response.ok().build();
    }
}
