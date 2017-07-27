package es.moki.ratelimitj.test.limiter.request;

import com.google.common.collect.ImmutableSet;
import es.moki.ratelimitj.core.limiter.request.ReactiveRequestRateLimiter;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.test.time.TimeBanditSupplier;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractReactiveRequestRateLimiterTest {

    private final TimeBanditSupplier timeBandit = new TimeBanditSupplier();

    protected abstract ReactiveRequestRateLimiter getRateLimiter(Set<RequestLimitRule> rules, TimeSupplier timeSupplier);

    @Test
    public void shouldLimitSingleWindowReactive() throws Exception {

        ImmutableSet<RequestLimitRule> rules = ImmutableSet.of(RequestLimitRule.of(10, TimeUnit.SECONDS, 5));
        ReactiveRequestRateLimiter rateLimiter = getRateLimiter(rules, timeBandit);

        Flux<Boolean> overLimitFlux = Flux
                .just("ip:127.0.1.5")
                .repeat(5)
                .flatMap(key -> {
                    timeBandit.addUnixTimeMilliSeconds(100);
                    return rateLimiter.overLimitWhenIncrementedReactive(key);
                });

        overLimitFlux.toStream().forEach(result -> assertThat(result).isFalse());

        assertThat(rateLimiter.overLimitWhenIncrementedReactive("ip:127.0.1.5").block()).isTrue();
    }

    @Test
    public void shouldGeLimitSingleWindowReactive() throws Exception {

        ImmutableSet<RequestLimitRule> rules = ImmutableSet.of(RequestLimitRule.of(10, TimeUnit.SECONDS, 5));
        ReactiveRequestRateLimiter rateLimiter = getRateLimiter(rules, timeBandit);

        Flux<Boolean> geLimitLimitFlux = Flux
                        .just("ip:127.0.1.2")
                        .repeat(4)
                        .flatMap(key -> {
                            timeBandit.addUnixTimeMilliSeconds(100);
                            return rateLimiter.overLimitWhenIncrementedReactive(key);
                        });

        geLimitLimitFlux.toStream().forEach(result -> assertThat(result).isFalse());

        assertThat(rateLimiter.overLimitWhenIncrementedReactive("ip:127.0.1.2").block()).isTrue();
    }

    @Test
    public void shouldLimitDualWindowReactive() throws Exception {

        ImmutableSet<RequestLimitRule> rules = ImmutableSet.of(RequestLimitRule.of(1, TimeUnit.SECONDS, 5), RequestLimitRule.of(10, TimeUnit.SECONDS, 10));
        ReactiveRequestRateLimiter rateLimiter = getRateLimiter(rules, timeBandit);

        Flux
                .just("ip:127.0.1.6")
                .repeat(5)
                .flatMap(key -> {
                    timeBandit.addUnixTimeMilliSeconds(100);
                    return rateLimiter.overLimitWhenIncrementedReactive(key);
                })
                .toStream()
                .forEach(result -> assertThat(result).isFalse());

        timeBandit.addUnixTimeMilliSeconds(1000L);

        Flux
                .just("ip:127.0.1.6")
                .repeat(5)
                .flatMap(key -> {
                    timeBandit.addUnixTimeMilliSeconds(100);
                    return rateLimiter.overLimitWhenIncrementedReactive(key);
                })
                .toStream()
                .forEach(result -> assertThat(result).isFalse());

        assertThat(rateLimiter.overLimitWhenIncrementedReactive("ip:127.0.1.6").block()).isTrue();
    }

    @Test
    public void shouldResetLimit() throws Exception {
        ImmutableSet<RequestLimitRule> rules = ImmutableSet.of(RequestLimitRule.of(60, TimeUnit.SECONDS, 1));
        ReactiveRequestRateLimiter rateLimiter = getRateLimiter(rules, timeBandit);

        String key =  "ip:127.1.0.1";

        assertThat(rateLimiter.overLimitWhenIncrementedReactive(key).block()).isFalse();
        assertThat(rateLimiter.overLimitWhenIncrementedReactive(key).block()).isTrue();

        assertThat(rateLimiter.resetLimitReactive(key).block()).isTrue();
        assertThat(rateLimiter.resetLimitReactive(key).block()).isFalse();

        assertThat(rateLimiter.overLimitWhenIncrementedReactive(key).block()).isFalse();
    }

}
