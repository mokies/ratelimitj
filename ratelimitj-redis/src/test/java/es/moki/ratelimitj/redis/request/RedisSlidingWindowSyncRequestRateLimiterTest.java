package es.moki.ratelimitj.redis.request;

import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.test.limiter.request.AbstractSyncRequestRateLimiterTest;
import es.moki.ratelimitj.test.time.TimeBanditSupplier;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableSet;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RedisSlidingWindowSyncRequestRateLimiterTest extends AbstractSyncRequestRateLimiterTest {

    private static RedisClient client;
    private static StatefulRedisConnection<String, String> connect;
    private static Logger LOG = LoggerFactory.getLogger(RedisSlidingWindowSyncRequestRateLimiterTest.class);

    @BeforeAll
    static void beforeAll() {
        client = RedisClient.create("redis://localhost");
        connect = client.connect();
    }

    @AfterAll
    @SuppressWarnings("FutureReturnValueIgnored")
    static void afterAll() {
        client.shutdownAsync();
    }

    @AfterEach
    void afterEach() {
        try (StatefulRedisConnection<String, String> connection = client.connect()) {
            connection.sync().flushdb();
        }
    }

    @Override
    protected RequestRateLimiter getRateLimiter(Set<RequestLimitRule> rules, TimeSupplier timeSupplier) {
        return new RedisSlidingWindowRequestRateLimiter(connect, rules, timeSupplier);
    }
    

    @Test
    void shouldCheckIncrementAnyway() throws Exception {
    	
        ImmutableSet<RequestLimitRule> rules = ImmutableSet.of(RequestLimitRule.of(20, TimeUnit.SECONDS, 5));
        TimeBanditSupplier timeBandit = new TimeBanditSupplier();
        
        RequestRateLimiter requestRateLimiter = getRateLimiter(rules, timeBandit);

        String key = "ip:127.0.0.5";

        timeBandit.addUnixTimeMilliSeconds(100L);
        // Counter should be 0 now
        assertThat(requestRateLimiter.overLimitWhenIncremented(key, 1)).isFalse();
        // Counter should be 1 now and 'false', meaning not over limit
        LOG.debug("About to print add 10 to go over.");
        timeBandit.addUnixTimeMilliSeconds(100L);
        assertThat(requestRateLimiter.incremementRegardless(key, 10)).isTrue();
        // Counter should be 11 now and 'true', meaning over limit
        
        LOG.debug("About to print add 1 that should be over..");
        timeBandit.addUnixTimeMilliSeconds(100L);
        assertThat(requestRateLimiter.overLimitWhenIncremented(key, 1)).isTrue();
        // Counter should be 11 now and 'true', meaning over limit
        // If the increment regardless worked, then the value would have been 2
        // which would not be under the limit.
    }
    
}
