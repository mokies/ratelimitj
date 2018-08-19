package es.moki.ratelimitj.test.limiter.request;

import com.google.common.collect.ImmutableSet;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.test.time.TimeBanditSupplier;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public abstract class AbstractSyncRequestRateLimiterTest {

    private final TimeBanditSupplier timeBandit = new TimeBanditSupplier();

    protected abstract RequestRateLimiter getRateLimiter(Set<RequestLimitRule> rules, TimeSupplier timeSupplier);

    @Test
    void shouldLimitSingleWindowSync()  {

        ImmutableSet<RequestLimitRule> rules = ImmutableSet.of(RequestLimitRule.of(Duration.ofSeconds(10), 5));
        RequestRateLimiter requestRateLimiter = getRateLimiter(rules, timeBandit);

        IntStream.rangeClosed(1, 5).forEach(value -> {
            timeBandit.addUnixTimeMilliSeconds(1000L);
            assertThat(requestRateLimiter.overLimitWhenIncremented("ip:127.0.1.1")).isFalse();
        });

        assertThat(requestRateLimiter.overLimitWhenIncremented("ip:127.0.1.1")).isTrue();
    }

    @Test
    void shouldGeLimitSingleWindowSync() {

        ImmutableSet<RequestLimitRule> rules = ImmutableSet.of(RequestLimitRule.of(Duration.ofSeconds(10), 5));
        RequestRateLimiter requestRateLimiter = getRateLimiter(rules, timeBandit);

        IntStream.rangeClosed(1, 4).forEach(value -> {
            timeBandit.addUnixTimeMilliSeconds(1000L);
            assertThat(requestRateLimiter.geLimitWhenIncremented("ip:127.0.1.2")).isFalse();
        });

        assertThat(requestRateLimiter.geLimitWhenIncremented("ip:127.0.1.2")).isTrue();
    }

    @Test
    void shouldLimitWithWeightSingleWindowSync() {

        ImmutableSet<RequestLimitRule> rules = ImmutableSet.of(RequestLimitRule.of(Duration.ofSeconds(10), 10));
        RequestRateLimiter requestRateLimiter = getRateLimiter(rules, timeBandit);

        IntStream.rangeClosed(1, 5).forEach(value -> {
            timeBandit.addUnixTimeMilliSeconds(1000L);
            assertThat(requestRateLimiter.overLimitWhenIncremented("ip:127.0.1.2", 2)).isFalse();
        });

        assertThat(requestRateLimiter.overLimitWhenIncremented("ip:127.0.1.2", 2)).isTrue();
    }

    @Test
    void shouldLimitSingleWindowSyncWithMultipleKeys() {

        ImmutableSet<RequestLimitRule> rules = ImmutableSet.of(RequestLimitRule.of(Duration.ofSeconds(10), 5));
        RequestRateLimiter requestRateLimiter = getRateLimiter(rules, timeBandit);

        IntStream.rangeClosed(1, 5).forEach(value -> {
            timeBandit.addUnixTimeMilliSeconds(1000L);
            IntStream.rangeClosed(1, 10).forEach(
                    keySuffix -> assertThat(requestRateLimiter.overLimitWhenIncremented("ip:127.0.0." + keySuffix)).isFalse());
        });

        IntStream.rangeClosed(1, 10).forEach(
                keySuffix -> assertThat(requestRateLimiter.overLimitWhenIncremented("ip:127.0.0." + keySuffix)).isTrue());

        timeBandit.addUnixTimeMilliSeconds(5000L);
        IntStream.rangeClosed(1, 10).forEach(
                keySuffix -> assertThat(requestRateLimiter.overLimitWhenIncremented("ip:127.0.0." + keySuffix)).isFalse());
    }

    @Test
    void shouldResetLimit() {
        ImmutableSet<RequestLimitRule> rules = ImmutableSet.of(RequestLimitRule.of(Duration.ofSeconds(60), 1));
        RequestRateLimiter requestRateLimiter = getRateLimiter(rules, timeBandit);

        String key = "ip:127.1.0.1";
        assertThat(requestRateLimiter.overLimitWhenIncremented(key)).isFalse();
        assertThat(requestRateLimiter.overLimitWhenIncremented(key)).isTrue();

        assertThat(requestRateLimiter.resetLimit(key)).isTrue();
        assertThat(requestRateLimiter.resetLimit(key)).isFalse();

        assertThat(requestRateLimiter.overLimitWhenIncremented(key)).isFalse();
    }

}
