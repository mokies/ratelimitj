package es.moki.aerospike.extensions;

import com.aerospike.client.AerospikeClient;
import es.moki.ratelimitj.aerospike.request.AerospikeContext;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class AerospikeConnectionSetup implements BeforeAllCallback, AfterAllCallback{

  private static AerospikeContext aerospikeContext;

  @Override
  @SuppressWarnings("FutureReturnValueIgnored")
  public void afterAll(ExtensionContext context) {
    aerospikeContext.aerospikeClient.close();
  }


  @Override
  public void beforeAll(ExtensionContext context) {
    AerospikeClient aerospikeClient = AerospikeClientFactory.getAerospikeClient();
    aerospikeContext = new AerospikeContext(aerospikeClient,"test","test");
  }

  public AerospikeContext getAerospikeContext() {
    return aerospikeContext;
  }

}
