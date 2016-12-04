package es.moki.ratelimij.dropwizard.filter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ResourceInfo;

public interface KeyProvider {

    String create(HttpServletRequest request, ResourceInfo resourceInfo);

}
