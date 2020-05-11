package es.moki.ratelimitj.aerospike.request;

import static java.util.Objects.requireNonNull;

import es.moki.ratelimitj.core.limiter.request.AbstractRequestRateLimiterFactory;
import es.moki.ratelimitj.core.limiter.request.ReactiveRequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import java.util.Set;

public class AerospikeRateLimiterFactory extends AbstractRequestRateLimiterFactory<AerospikeSlidingWindowRateLimiter> {
  private final AerospikeContext aerospikeContext;

  public AerospikeRateLimiterFactory(AerospikeContext aerospikeContext) {
    this.aerospikeContext = requireNonNull(aerospikeContext,"aerospikeContext can not be null");
  }

  @Override
  public RequestRateLimiter getInstance(Set<RequestLimitRule> rules) {
    return lookupInstance(rules);
  }

  @Override
  public ReactiveRequestRateLimiter getInstanceReactive(Set<RequestLimitRule> rules) {
    throw new RuntimeException("Aerospike reactive not yet implemented");
  }

  @Override
  protected AerospikeSlidingWindowRateLimiter create(Set<RequestLimitRule> rules) {
    return new AerospikeSlidingWindowRateLimiter(aerospikeContext,rules);
  }

  @Override
  public void close() {
  }

}
