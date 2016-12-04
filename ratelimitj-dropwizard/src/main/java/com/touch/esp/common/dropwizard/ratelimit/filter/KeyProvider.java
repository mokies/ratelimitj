package com.touch.esp.common.dropwizard.ratelimit.filter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ResourceInfo;

public interface KeyProvider {

    String create(HttpServletRequest request, ResourceInfo resourceInfo);

}
