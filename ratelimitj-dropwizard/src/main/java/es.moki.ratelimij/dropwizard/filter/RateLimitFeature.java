package es.moki.ratelimij.dropwizard.filter;

import com.google.common.collect.ImmutableSet;
import es.moki.ratelimij.dropwizard.RateLimitBundle;
import es.moki.ratelimitj.core.api.LimitRule;
import es.moki.ratelimitj.core.api.RateLimiterFactory;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;


@Provider
public class RateLimitFeature implements DynamicFeature {

    @Override
    public void configure(ResourceInfo resourceInfo,
                          FeatureContext context) {

        RateLimited rateLimited = resourceInfo.getResourceMethod().getAnnotation(RateLimited.class);
        if (rateLimited == null) {
            return;
        }

        RateLimiterFactory factory = (RateLimiterFactory) context.getConfiguration().getProperty(RateLimitBundle.PROPERTY_FACTORY);

        LimitRule rule = LimitRule.of(rateLimited.duration(), rateLimited.timeUnit(), rateLimited.limit());
        SimpleRateLimitFilter rateLimitFilter = new SimpleRateLimitFilter(factory.getInstance(ImmutableSet.of(rule)));
        context.register(rateLimitFilter);
    }
}
