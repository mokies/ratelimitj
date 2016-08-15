package es.moki.ratelimitj.internal.test;

import com.google.common.collect.ImmutableSet;
import es.moki.ratelimitj.core.api.LimitRule;
import es.moki.ratelimitj.core.api.RateLimiter;
import es.moki.ratelimitj.core.time.TimeBanditSupplier;
import es.moki.ratelimitj.core.time.TimeSupplier;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public abstract class AbstractSyncRateLimiterTest {

    private final TimeBanditSupplier timeBandit = new TimeBanditSupplier();

    protected abstract RateLimiter getRateLimiter(Set<LimitRule> rules, TimeSupplier timeSupplier);

    @Test
    public void shouldLimitSingleWindowSync() throws Exception {

        ImmutableSet<LimitRule> rules = ImmutableSet.of(LimitRule.of(10, TimeUnit.SECONDS, 5));
        RateLimiter rateLimiter = getRateLimiter(rules, timeBandit);

        IntStream.rangeClosed(1, 5).forEach(value -> {
            timeBandit.addUnixTimeMilliSeconds(1000L);
            assertThat(rateLimiter.overLimit("ip:127.0.1.5")).isFalse();
        });

        assertThat(rateLimiter.overLimit("ip:127.0.1.5")).isTrue();
    }

    @Test
    public void shouldLimitSingleWindowSyncWithMultipleKeys() throws Exception {

        ImmutableSet<LimitRule> rules = ImmutableSet.of(LimitRule.of(10, TimeUnit.SECONDS, 5));
        RateLimiter rateLimiter = getRateLimiter(rules, timeBandit);

        IntStream.rangeClosed(1, 5).forEach(value -> {
            timeBandit.addUnixTimeMilliSeconds(1000L);
            IntStream.rangeClosed(1, 10).forEach(
                    keySuffix -> assertThat(rateLimiter.overLimit("ip:127.0.0." + keySuffix)).isFalse());
        });

        IntStream.rangeClosed(1, 10).forEach(
                keySuffix -> assertThat(rateLimiter.overLimit("ip:127.0.0." + keySuffix)).isTrue());

        timeBandit.addUnixTimeMilliSeconds(5000L);
        IntStream.rangeClosed(1, 10).forEach(
                keySuffix -> assertThat(rateLimiter.overLimit("ip:127.0.0." + keySuffix)).isFalse());
    }

    @Test
    public void shouldResetLimit() {
        ImmutableSet<LimitRule> rules = ImmutableSet.of(LimitRule.of(60, TimeUnit.SECONDS, 1));
        RateLimiter rateLimiter = getRateLimiter(rules, timeBandit);

        String key =  "ip:127.1.0.1";
        assertThat(rateLimiter.overLimit(key)).isFalse();
        assertThat(rateLimiter.overLimit(key)).isTrue();

        assertThat(rateLimiter.resetLimit(key)).isTrue();
        assertThat(rateLimiter.resetLimit(key)).isFalse();

        assertThat(rateLimiter.overLimit(key)).isFalse();
    }

}
