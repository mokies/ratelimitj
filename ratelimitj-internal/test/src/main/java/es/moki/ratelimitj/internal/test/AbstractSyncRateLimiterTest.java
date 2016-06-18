package es.moki.ratelimitj.internal.test;

import com.google.common.collect.ImmutableSet;
import es.moki.ratelimitj.api.LimitRule;
import es.moki.ratelimitj.api.RateLimiter;
import es.moki.ratelimitj.core.time.time.TimeBanditSupplier;
import es.moki.ratelimitj.core.time.time.TimeSupplier;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public abstract class AbstractSyncRateLimiterTest {

    private TimeBanditSupplier timeBandit = new TimeBanditSupplier();

    protected abstract RateLimiter getRateLimiter(Set<LimitRule> rules, TimeSupplier timeSupplier);

    @Test
    public void shouldLimitSingleWindowSync() throws Exception {

        ImmutableSet<LimitRule> rules = ImmutableSet.of(LimitRule.of(10, TimeUnit.SECONDS, 5));
        RateLimiter rateLimiter = getRateLimiter(rules, timeBandit);

        IntStream.rangeClosed(1, 5).forEach(value -> {
            timeBandit.addUnixTimeMilliSeconds(1000L);
            assertThat(rateLimiter.overLimit("ip:127.0.0.5")).isFalse();
        });

        assertThat(rateLimiter.overLimit("ip:127.0.0.5")).isTrue();
    }

}
