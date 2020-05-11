package es.moki.aerospike.request;

import es.moki.aerospike.extensions.AerospikeConnectionSetup;
import es.moki.ratelimitj.aerospike.request.AerospikeSlidingWindowRateLimiter;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.test.limiter.request.AbstractSyncRequestRateLimiterPerformanceTest;
import java.util.Set;
import org.junit.jupiter.api.extension.RegisterExtension;

public class AerospikeSlidingWindowRequestRateLimiterPerformanceTest extends
    AbstractSyncRequestRateLimiterPerformanceTest {

  @RegisterExtension
  static AerospikeConnectionSetup aerospikeConnectionSetup = new AerospikeConnectionSetup();

  @Override
  protected RequestRateLimiter getRateLimiter(
      Set<RequestLimitRule> rules, TimeSupplier timeSupplier) {
    return new AerospikeSlidingWindowRateLimiter(aerospikeConnectionSetup.getAerospikeContext(),
        rules, timeSupplier);
  }
}
