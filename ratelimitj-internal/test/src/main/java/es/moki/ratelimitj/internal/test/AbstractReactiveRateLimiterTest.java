package es.moki.ratelimitj.internal.test;

import com.google.common.collect.ImmutableSet;
import es.moki.ratelimitj.core.api.LimitRule;
import es.moki.ratelimitj.core.api.ReactiveRateLimiter;
import es.moki.ratelimitj.core.time.TimeBanditSupplier;
import es.moki.ratelimitj.core.time.TimeSupplier;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractReactiveRateLimiterTest {

    private final TimeBanditSupplier timeBandit = new TimeBanditSupplier();

    protected abstract ReactiveRateLimiter getRateLimiter(Set<LimitRule> rules, TimeSupplier timeSupplier);

    @Test
    public void shouldLimitSingleWindowSync() throws Exception {

        ImmutableSet<LimitRule> rules = ImmutableSet.of(LimitRule.of(10, TimeUnit.SECONDS, 5));
        ReactiveRateLimiter rateLimiter = getRateLimiter(rules, timeBandit);

        Flux<Boolean> overLimitFlux = Flux
                .just("ip:127.0.1.5")
                .repeat(5)
                .flatMap(key -> {
                    timeBandit.addUnixTimeMilliSeconds(100);
                    return rateLimiter.overLimitReactive(key);
                });

        overLimitFlux.toStream().forEach(result -> assertThat(result).isFalse());

        assertThat(rateLimiter.overLimitReactive("ip:127.0.1.5").block()).isTrue();
    }

    @Test
    public void shouldLimitDualWindowAsync() throws Exception {

        ImmutableSet<LimitRule> rules = ImmutableSet.of(LimitRule.of(1, TimeUnit.SECONDS, 5), LimitRule.of(10, TimeUnit.SECONDS, 10));
        ReactiveRateLimiter rateLimiter = getRateLimiter(rules, timeBandit);

        Flux
                .just("ip:127.0.1.6")
                .repeat(5)
                .flatMap(key -> {
                    timeBandit.addUnixTimeMilliSeconds(100);
                    return rateLimiter.overLimitReactive(key);
                })
                .toStream()
                .forEach(result -> assertThat(result).isFalse());

        timeBandit.addUnixTimeMilliSeconds(1000L);

        Flux
                .just("ip:127.0.1.6")
                .repeat(5)
                .flatMap(key -> {
                    timeBandit.addUnixTimeMilliSeconds(100);
                    return rateLimiter.overLimitReactive(key);
                })
                .toStream()
                .forEach(result -> assertThat(result).isFalse());

        assertThat(rateLimiter.overLimitReactive("ip:127.0.1.6").block()).isTrue();
    }

    @Test
    public void shouldResetLimit() throws Exception {
        ImmutableSet<LimitRule> rules = ImmutableSet.of(LimitRule.of(60, TimeUnit.SECONDS, 1));
        ReactiveRateLimiter rateLimiter = getRateLimiter(rules, timeBandit);

        String key =  "ip:127.1.0.1";

        assertThat(rateLimiter.overLimitReactive(key).block()).isFalse();
        assertThat(rateLimiter.overLimitReactive(key).block()).isTrue();

        assertThat(rateLimiter.resetLimitReactive(key).block()).isTrue();
        assertThat(rateLimiter.resetLimitReactive(key).block()).isFalse();

        assertThat(rateLimiter.overLimitReactive(key).block()).isFalse();
    }

}
