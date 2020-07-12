package es.moki.aerospike.request;

import com.aerospike.client.AerospikeClient;
import es.moki.aerospike.extensions.AerospikeClientFactory;
import es.moki.ratelimitj.aerospike.request.AerospikeContext;
import es.moki.ratelimitj.aerospike.request.AerospikeSlidingWindowRateLimiter;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.test.limiter.request.AbstractSyncRequestRateLimiterTest;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class AerospikeSlidingWindowRequestRateLimiterTest extends AbstractSyncRequestRateLimiterTest {

  private static AerospikeContext aerospikeContext;

  @AfterAll
  static void afterAll() {
    aerospikeContext.aerospikeClient.close();
  }

  @BeforeAll
  static void beforeAll() {
    AerospikeClient aerospikeClient = AerospikeClientFactory.getAerospikeClient();
    aerospikeContext = new AerospikeContext(aerospikeClient,"test","test");
  }

  @Override
  protected RequestRateLimiter getRateLimiter(Set<RequestLimitRule> rules, TimeSupplier timeSupplier) {
    return new AerospikeSlidingWindowRateLimiter(aerospikeContext,rules, timeSupplier);
  }
}
