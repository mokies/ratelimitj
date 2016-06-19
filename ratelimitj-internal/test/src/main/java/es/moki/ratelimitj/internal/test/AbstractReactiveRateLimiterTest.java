package es.moki.ratelimitj.internal.test;

import com.google.common.collect.ImmutableSet;
import es.moki.ratelimitj.api.LimitRule;
import es.moki.ratelimitj.api.ReactiveRateLimiter;
import es.moki.ratelimitj.core.time.time.TimeBanditSupplier;
import es.moki.ratelimitj.core.time.time.TimeSupplier;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractReactiveRateLimiterTest {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private TimeBanditSupplier timeBandit = new TimeBanditSupplier();

    protected abstract ReactiveRateLimiter getRateLimiter(Set<LimitRule> rules, TimeSupplier timeSupplier);

    @Test
    public void shouldLimitSingleWindowSync() throws Exception {

        ImmutableSet<LimitRule> rules = ImmutableSet.of(LimitRule.of(10, TimeUnit.SECONDS, 5));
        ReactiveRateLimiter rateLimiter = getRateLimiter(rules, timeBandit);

        Observable<Boolean> overLimitObservable = Observable
                .just("ip:127.0.1.5")
                .repeat(5)
                .flatMap(key -> {
                    timeBandit.addUnixTimeMilliSeconds(100);
                    return rateLimiter.overLimitReactive(key);
                });

        overLimitObservable.toBlocking().subscribe(result -> assertThat(result).isFalse());

        rateLimiter.overLimitReactive("ip:127.0.1.5").toBlocking().subscribe(result -> assertThat(result).isTrue());
    }

    @Test
    public void shouldLimitDualWindowAsync() throws Exception {

        ImmutableSet<LimitRule> rules = ImmutableSet.of(LimitRule.of(1, TimeUnit.SECONDS, 5), LimitRule.of(10, TimeUnit.SECONDS, 10));
        ReactiveRateLimiter rateLimiter = getRateLimiter(rules, timeBandit);

        Observable
                .just("ip:127.0.1.6")
                .repeat(5)
                .flatMap(key -> {
                    timeBandit.addUnixTimeMilliSeconds(100);
                    return rateLimiter.overLimitReactive(key);
                })
                .toBlocking()
                .subscribe(result -> assertThat(result).isFalse());

        timeBandit.addUnixTimeMilliSeconds(1000L);

        Observable
                .just("ip:127.0.1.6")
                .repeat(5)
                .flatMap(key -> {
                    timeBandit.addUnixTimeMilliSeconds(100);
                    return rateLimiter.overLimitReactive(key);
                })
                .toBlocking()
                .subscribe(result -> assertThat(result).isFalse());

        rateLimiter.overLimitReactive("ip:127.0.1.6").toBlocking().subscribe(result -> assertThat(result).isTrue());
    }

}
