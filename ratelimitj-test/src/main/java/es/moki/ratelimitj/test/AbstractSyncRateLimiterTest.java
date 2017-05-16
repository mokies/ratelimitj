package es.moki.ratelimitj.test;

import com.google.common.collect.ImmutableSet;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.test.time.TimeBanditSupplier;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public abstract class AbstractSyncRateLimiterTest {

    private final TimeBanditSupplier timeBandit = new TimeBanditSupplier();

    protected abstract RequestRateLimiter getRateLimiter(Set<RequestLimitRule> rules, TimeSupplier timeSupplier);

    @Test
    public void shouldLimitSingleWindowSync() throws Exception {

        ImmutableSet<RequestLimitRule> rules = ImmutableSet.of(RequestLimitRule.of(10, TimeUnit.SECONDS, 5));
        RequestRateLimiter requestRateLimiter = getRateLimiter(rules, timeBandit);

        IntStream.rangeClosed(1, 5).forEach(value -> {
            timeBandit.addUnixTimeMilliSeconds(1000L);
            assertThat(requestRateLimiter.overLimit("ip:127.0.1.5")).isFalse();
        });

        assertThat(requestRateLimiter.overLimit("ip:127.0.1.5")).isTrue();
    }

    @Test
    public void shouldLimitSingleWindowSyncWithMultipleKeys() throws Exception {

        ImmutableSet<RequestLimitRule> rules = ImmutableSet.of(RequestLimitRule.of(10, TimeUnit.SECONDS, 5));
        RequestRateLimiter requestRateLimiter = getRateLimiter(rules, timeBandit);

        IntStream.rangeClosed(1, 5).forEach(value -> {
            timeBandit.addUnixTimeMilliSeconds(1000L);
            IntStream.rangeClosed(1, 10).forEach(
                    keySuffix -> assertThat(requestRateLimiter.overLimit("ip:127.0.0." + keySuffix)).isFalse());
        });

        IntStream.rangeClosed(1, 10).forEach(
                keySuffix -> assertThat(requestRateLimiter.overLimit("ip:127.0.0." + keySuffix)).isTrue());

        timeBandit.addUnixTimeMilliSeconds(5000L);
        IntStream.rangeClosed(1, 10).forEach(
                keySuffix -> assertThat(requestRateLimiter.overLimit("ip:127.0.0." + keySuffix)).isFalse());
    }

    @Test
    public void shouldResetLimit() {
        ImmutableSet<RequestLimitRule> rules = ImmutableSet.of(RequestLimitRule.of(60, TimeUnit.SECONDS, 1));
        RequestRateLimiter requestRateLimiter = getRateLimiter(rules, timeBandit);

        String key = "ip:127.1.0.1";
        assertThat(requestRateLimiter.overLimit(key)).isFalse();
        assertThat(requestRateLimiter.overLimit(key)).isTrue();

        assertThat(requestRateLimiter.resetLimit(key)).isTrue();
        assertThat(requestRateLimiter.resetLimit(key)).isFalse();

        assertThat(requestRateLimiter.overLimit(key)).isFalse();
    }

}
