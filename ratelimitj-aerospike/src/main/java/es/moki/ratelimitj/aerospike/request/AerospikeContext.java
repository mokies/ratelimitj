package es.moki.ratelimitj.aerospike.request;

import static java.util.Objects.requireNonNull;

import com.aerospike.client.AerospikeClient;


public class AerospikeContext {

  public final AerospikeClient aerospikeClient;

  public final String setName;

  public final String nameSpace;

  public AerospikeContext(AerospikeClient aerospikeClient, String setName, String nameSpace) {
    this.aerospikeClient = requireNonNull(aerospikeClient, "aerospike client can not be null");
    this.setName = requireNonNull(setName, "set name can not be null");
    this.nameSpace = requireNonNull(nameSpace, "set name can not be null");
  }

}
