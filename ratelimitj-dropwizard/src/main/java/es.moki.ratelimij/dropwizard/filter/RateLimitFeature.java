package es.moki.ratelimij.dropwizard.filter;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;


// TODO complete feature
@Provider
public class RateLimitFeature implements DynamicFeature {
    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        if (resourceInfo.getResourceMethod().getAnnotation(RateLimited.class) != null) {
            context.register(RateLimitFilter.class);
        }
    }
}
