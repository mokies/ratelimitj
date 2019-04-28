package es.moki.ratelimitj.test.limiter.request;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.test.time.TimeBanditSupplier;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
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
    void shouldLimitSingleWindowSyncWithKeySpecificRules() {

        RequestLimitRule rule1 = RequestLimitRule.of(Duration.ofSeconds(10), 5).matchingKeys("ip:127.9.0.0");
        RequestLimitRule rule2 = RequestLimitRule.of(Duration.ofSeconds(10), 10);

        RequestRateLimiter requestRateLimiter = getRateLimiter(ImmutableSet.of(rule1, rule2), timeBandit);

        IntStream.rangeClosed(1, 5).forEach(value -> {
            timeBandit.addUnixTimeMilliSeconds(1000L);
            assertThat(requestRateLimiter.overLimitWhenIncremented("ip:127.9.0.0")).isFalse();
        });
        assertThat(requestRateLimiter.overLimitWhenIncremented("ip:127.9.0.0")).isTrue();

        IntStream.rangeClosed(1, 10).forEach(value -> assertThat(requestRateLimiter.overLimitWhenIncremented("ip:127.9.1.0")).isFalse());
        assertThat(requestRateLimiter.overLimitWhenIncremented("ip:127.9.1.0")).isTrue();
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


    @Test
    void shouldRateLimitOverTime() {
        RequestLimitRule rule1 = RequestLimitRule.of(Duration.ofSeconds(5), 250).withPrecision(Duration.ofSeconds(1)).matchingKeys("ip:127.3.9.3");
        RequestRateLimiter requestRateLimiter = getRateLimiter(ImmutableSet.of(rule1), timeBandit);
        AtomicLong timeOfLastOperation = new AtomicLong();

        IntStream.rangeClosed(1, 50).forEach(loop -> {

            IntStream.rangeClosed(1, 250).forEach(value -> {
                timeBandit.addUnixTimeMilliSeconds(14L);
                boolean overLimit = requestRateLimiter.overLimitWhenIncremented("ip:127.3.9.3");
                if (overLimit) {
                    long timeSinceLastOperation = timeBandit.get() - timeOfLastOperation.get();
                    assertThat(timeSinceLastOperation).isLessThan(3);
                } else {
                    timeOfLastOperation.set(timeBandit.get());
                }
            });

        });
    }

    @Test @Disabled
    void shouldPreventThunderingHerdWithPrecision() {

        RequestLimitRule rule1 = RequestLimitRule.of(Duration.ofSeconds(5), 250).withPrecision(Duration.ofSeconds(1)).matchingKeys("ip:127.9.9.9");
        RequestRateLimiter requestRateLimiter = getRateLimiter(ImmutableSet.of(rule1), timeBandit);
        Map<Long, Integer> underPerSecond = new LinkedHashMap<>();
        Map<Long, Integer> overPerSecond = new HashMap<>();

        IntStream.rangeClosed(1, 50).forEach(loop -> {

            IntStream.rangeClosed(1, 250).forEach(value -> {
                timeBandit.addUnixTimeMilliSeconds(14L);
                boolean overLimit = requestRateLimiter.overLimitWhenIncremented("ip:127.9.9.9");
                if (!overLimit) {
                    underPerSecond.merge(timeBandit.get(), 1, Integer::sum);
                } else {
                    overPerSecond.merge(timeBandit.get(), 1, Integer::sum);
                }
            });

        });

        Set<Long> allSeconds = Sets.newTreeSet(Sets.union(underPerSecond.keySet(), overPerSecond.keySet()));

        allSeconds.forEach((k)->System.out.println("Time seconds : " + k + " under count : " + underPerSecond.get(k) + " over count : " + overPerSecond.get(k)));
    }


}
