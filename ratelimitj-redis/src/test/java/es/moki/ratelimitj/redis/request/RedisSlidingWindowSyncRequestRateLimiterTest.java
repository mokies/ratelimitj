package es.moki.ratelimitj.redis.request;

import com.google.common.collect.ImmutableSet;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.redis.extensions.RedisStandaloneConnectionSetupExtension;
import es.moki.ratelimitj.test.limiter.request.AbstractSyncRequestRateLimiterTest;
import es.moki.ratelimitj.test.time.TimeBanditSupplier;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;


public class RedisSlidingWindowSyncRequestRateLimiterTest extends AbstractSyncRequestRateLimiterTest {

    private final TimeBanditSupplier timeBandit = new TimeBanditSupplier();

    @RegisterExtension
    static RedisStandaloneConnectionSetupExtension extension = new RedisStandaloneConnectionSetupExtension();

    @Override
    protected RequestRateLimiter getRateLimiter(Set<RequestLimitRule> rules, TimeSupplier timeSupplier) {
        return new RedisSlidingWindowRequestRateLimiter(extension.getScriptingReactiveCommands(), extension.getKeyReactiveCommands(), rules, timeSupplier);
    }

    @Test
    void shouldRateLimitOverTimeWithBackoff() {
        RequestLimitRule rule1 = RequestLimitRule.of(Duration.ofSeconds(5), 10).withPrecision(Duration.ofSeconds(1)).withBackoff(Duration.ofSeconds(30));
        RequestRateLimiter requestRateLimiter = getRateLimiter(ImmutableSet.of(rule1), timeBandit);
        AtomicLong timeOfLastOperation = new AtomicLong(timeBandit.get());

        IntStream.rangeClosed(1, 20).forEach(loop -> {

            IntStream.rangeClosed(1, 250).forEach(value -> {
                timeBandit.addUnixTimeMilliSeconds(14L);
                boolean overLimit = requestRateLimiter.overLimitWhenIncremented("ip:127.3.9.3");

                if (overLimit) {
                    long timeSinceLastOperation = timeBandit.get() - timeOfLastOperation.get();
                    assertThat(timeSinceLastOperation).isLessThanOrEqualTo(30);
                } else {
                    timeOfLastOperation.set(timeBandit.get());
                }
            });
        });
    }

    @Test
    void shouldRateLimitForSingleRuleWithBackoff() {
        RequestLimitRule rule1 = RequestLimitRule.of(Duration.ofSeconds(5), 10).withPrecision(Duration.ofSeconds(1)).withBackoff(Duration.ofSeconds(30));
        RequestRateLimiter requestRateLimiter = getRateLimiter(ImmutableSet.of(rule1), timeBandit);

        // Allow first 10 requests
        IntStream.rangeClosed(1, 10).forEach(loop -> {
            timeBandit.addUnixTimeMilliSeconds(200L);
            assertThat(requestRateLimiter.overLimitWhenIncremented("ip:127.3.9.3")).isFalse();
        });

        // Block for the next 30 seconds
        IntStream.rangeClosed(1, 6).forEach(loop -> {
            timeBandit.addUnixTimeMilliSeconds(TimeUnit.SECONDS.toMillis(5));
            assertThat(requestRateLimiter.overLimitWhenIncremented("ip:127.3.9.3")).isTrue();
        });

        // Allow again after the backoff is gone
        timeBandit.addUnixTimeMilliSeconds(TimeUnit.SECONDS.toMillis(1));
        assertThat(requestRateLimiter.overLimitWhenIncremented("ip:127.3.9.3")).isFalse();
    }
}
