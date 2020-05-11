package es.moki.ratelimitj.aerospike.request;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Key;
import com.aerospike.client.Operation;
import com.aerospike.client.Record;
import com.aerospike.client.Value;
import com.aerospike.client.Value.StringValue;
import com.aerospike.client.cdt.MapOperation;
import com.aerospike.client.cdt.MapPolicy;
import com.aerospike.client.cdt.MapReturnType;
import com.aerospike.client.policy.RecordExistsAction;
import com.aerospike.client.policy.WritePolicy;
import com.google.common.collect.Maps;
import es.moki.ratelimitj.core.limiter.request.DefaultRequestLimitRulesSupplier;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.core.time.SystemTimeSupplier;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.inmemory.request.SavedKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static es.moki.ratelimitj.core.RateLimitUtils.coalesce;
import static java.util.Objects.requireNonNull;

@ParametersAreNonnullByDefault
@ThreadSafe
public class AerospikeSlidingWindowRateLimiter  implements RequestRateLimiter {

  private static final Logger LOG = LoggerFactory.getLogger(AerospikeSlidingWindowRateLimiter.class);
  private static final String BIN_NAME = "rate_limit";

  private final AerospikeContext aerospikeContext;
  private final DefaultRequestLimitRulesSupplier rulesSupplier;
  private final TimeSupplier timeSupplier;

   public AerospikeSlidingWindowRateLimiter(AerospikeContext aerospikeContext,Set<RequestLimitRule> rules){
    this(aerospikeContext,rules,new SystemTimeSupplier());
   }

  public AerospikeSlidingWindowRateLimiter(AerospikeContext aerospikeContext, Set<RequestLimitRule> rules, TimeSupplier timeSupplier) {
    this.aerospikeContext = requireNonNull(aerospikeContext, "aerospikeContext can not be null");
    requireNonNull(rules, "rules can not be null");
    if (rules.isEmpty()) {
      throw new IllegalArgumentException("at least one rule must be provided");
    }
    this.timeSupplier = requireNonNull(timeSupplier, "time supplier can not be null");
    this.rulesSupplier = new DefaultRequestLimitRulesSupplier(rules);
  }

  @Override
  public boolean overLimitWhenIncremented(String key) {
    return overLimitWhenIncremented(key, 1);
  }

  @Override
  public boolean overLimitWhenIncremented(String key, int weight) {
    return eqOrGeLimit(key, weight, true);
  }

  @Override
  public boolean geLimitWhenIncremented(String key) {
    return geLimitWhenIncremented(key, 1);
  }

  @Override
  public boolean geLimitWhenIncremented(String key, int weight) {
    return eqOrGeLimit(key, weight, false);
  }


  @Override
  public boolean resetLimit(String key) {
    return aerospikeContext.aerospikeClient.delete(aerospikeContext.aerospikeClient.getWritePolicyDefault(),
        new Key(aerospikeContext.nameSpace,aerospikeContext.setName,key)) ;
  }

  private Map<String, Long> getMap(final String userKey) {
    AerospikeClient aerospikeClient = aerospikeContext.aerospikeClient;
    final Key key = new Key(aerospikeContext.nameSpace,aerospikeContext.setName,userKey);
    final Record record = aerospikeContext.aerospikeClient.get(aerospikeClient.getReadPolicyDefault(),key);

    return record != null ? (Map<String, Long>)record.getMap(BIN_NAME): Maps.newHashMap();
  }

  private boolean eqOrGeLimit(String key, int weight, boolean strictlyGreater) {

    final long now = timeSupplier.get();
    final Set<RequestLimitRule> rules = rulesSupplier.getRules(key);
    final int longestDuration = rules.stream().map(RequestLimitRule::getDurationSeconds).reduce(Integer::max).orElse(0);
    List<SavedKey> savedKeys = new ArrayList<>(rules.size());

    Map<String, Long> asKeyMap = getMap(key);
    boolean geLimit = false;
    Map<SavedKey,List<Operation>> savedKeyListMap = new HashMap<>();
    for (RequestLimitRule rule : rules) {

      SavedKey savedKey = new SavedKey(now, rule.getDurationSeconds(), rule.getPrecisionSeconds());
      savedKeys.add(savedKey);

      Long oldTs = asKeyMap.get(savedKey.tsKey);

      oldTs = oldTs != null ? oldTs : savedKey.trimBefore;

      if (oldTs > now) {
        // don't write in the past
        return true;
      }



      // compute no of requests in given window;
      long curr = 0L;
      for (long block = savedKey.trimBefore; block <= savedKey.blockId; block++) {
        String bkey = savedKey.countKey + block;
        curr = curr + asKeyMap.getOrDefault(bkey,0L);
      }


      // discover what needs to be cleaned up
      List<Value> keyNeedToDeleted = new ArrayList<>();
      long trim = Math.min(savedKey.trimBefore, oldTs + savedKey.blocks);

      for (long oldBlock = oldTs; oldBlock <= trim - 1; oldBlock++) {
        keyNeedToDeleted.add(new StringValue(savedKey.countKey + oldBlock));
      }


      if(!keyNeedToDeleted.isEmpty()){
        Operation operation = MapOperation.removeByKeyList(BIN_NAME,keyNeedToDeleted, MapReturnType.NONE);
        List<Operation> operations = savedKeyListMap.getOrDefault(savedKey,new ArrayList<>());
        operations.add(operation);
        savedKeyListMap.put(savedKey,operations);
      }


      // check our limits
      long count = coalesce(curr, 0L) + weight;
      if (count > rule.getLimit()) {
        return true; // over limit, don't record request
      } else if (!strictlyGreater && count == rule.getLimit()) {
        geLimit = true; // at limit, do record request
      }
      Operation operation = MapOperation.increment(MapPolicy.Default,BIN_NAME,Value.get(savedKey.countKey + savedKey.blockId),Value.get(weight));
      List<Operation> operations = savedKeyListMap.getOrDefault(savedKey,new ArrayList<>());
      operations.add(operation);
      savedKeyListMap.put(savedKey,operations);
    }

    // there is enough resources, update the counts and delete map key
    AerospikeClient aerospikeClient = aerospikeContext.aerospikeClient;
    final WritePolicy updationWritePolicy = new WritePolicy(
        aerospikeContext.aerospikeClient.getWritePolicyDefault());
    updationWritePolicy.recordExistsAction = RecordExistsAction.UPDATE;
    updationWritePolicy.expiration = longestDuration;
    for (SavedKey savedKey : savedKeys) {
      //update the current timestamp, count, and bucket count and delete key
      LOG.debug("Update key {},{}",key,savedKey);
      List<Operation> operations = savedKeyListMap.getOrDefault(savedKey,new ArrayList<>());

      operations.add(MapOperation.put(MapPolicy.Default,BIN_NAME,Value.get(savedKey.tsKey),Value.get(savedKey.trimBefore)));

      Operation[] operationArray = new Operation[operations.size()];

      operationArray = operations.toArray(operationArray);

      aerospikeClient.operate(updationWritePolicy,new Key(aerospikeContext.nameSpace,aerospikeContext.setName,key),operationArray);
    }
    return geLimit;
  }


}
