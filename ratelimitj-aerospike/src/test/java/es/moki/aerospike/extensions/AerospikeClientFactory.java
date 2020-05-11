package es.moki.aerospike.extensions;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Host;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.CommitLevel;
import com.aerospike.client.policy.Policy;
import com.aerospike.client.policy.ReadModeAP;
import com.aerospike.client.policy.Replica;
import com.aerospike.client.policy.WritePolicy;
import java.util.concurrent.Executors;

public class AerospikeClientFactory {

  public static AerospikeClient getAerospikeClient() {

    Policy readPolicy = new Policy();
    readPolicy.maxRetries = 1;
    readPolicy.readModeAP = ReadModeAP.ONE;
    readPolicy.replica = Replica.MASTER_PROLES;
    readPolicy.sleepBetweenRetries = 10;
    readPolicy.totalTimeout = 100;
    readPolicy.sendKey = true;

    WritePolicy writePolicy = new WritePolicy();
    writePolicy.maxRetries = 1;
    writePolicy.readModeAP = ReadModeAP.ALL;
    writePolicy.replica = Replica.MASTER_PROLES;
    writePolicy.sleepBetweenRetries = 10;
    writePolicy.commitLevel = CommitLevel.COMMIT_ALL;
    writePolicy.totalTimeout = 100;
    writePolicy.sendKey = true;

    ClientPolicy clientPolicy = new ClientPolicy();
    clientPolicy.maxConnsPerNode = 2;
    clientPolicy.readPolicyDefault = readPolicy;
    clientPolicy.writePolicyDefault = writePolicy;
    clientPolicy.failIfNotConnected = true;
    clientPolicy.threadPool = Executors.newFixedThreadPool(5);

    AerospikeClient aerospikeClient = new AerospikeClient(clientPolicy,
        new Host("172.28.128.3", 3000));
    return aerospikeClient;
  }
}
