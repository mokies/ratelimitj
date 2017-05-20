package es.moki.ratelimij.dropwizard.filter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.SecurityContext;

public interface KeyProvider {

    String create(HttpServletRequest request, ResourceInfo resourceInfo, SecurityContext securityContext);

}
