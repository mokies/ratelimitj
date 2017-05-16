package es.moki.ratelimitj.test.limiter.request;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.core.time.TimeSupplier;
import es.moki.ratelimitj.test.time.TimeBanditSupplier;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;


public abstract class AbstractSyncRequestRateLimiterPerformanceTest {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final TimeBanditSupplier timeBandit = new TimeBanditSupplier();

    protected abstract RequestRateLimiter getRateLimiter(Set<RequestLimitRule> rules, TimeSupplier timeSupplier);

    @Test
    public void shouldLimitDualWindowSyncTimed() throws Exception {

        Stopwatch watch = Stopwatch.createStarted();

        ImmutableSet<RequestLimitRule> rules =
                ImmutableSet.of(RequestLimitRule.of(2, TimeUnit.SECONDS, 100), RequestLimitRule.of(10, TimeUnit.SECONDS, 100));
        RequestRateLimiter requestRateLimiter = getRateLimiter(rules, timeBandit);
        Random rand = new Random();

        int total = 10_000;
        IntStream.rangeClosed(1, total).map(i -> rand.nextInt(128)).forEach(value -> {
            timeBandit.addUnixTimeMilliSeconds(200L);
            requestRateLimiter.overLimitWhenIncremented("ip:127.0.0." + value);
        });

        double transactionsPerSecond = Math.ceil((double) total / watch.elapsed(TimeUnit.MILLISECONDS) * 1000);

        log.info("total time {} checks {}/sec", watch.stop(), NumberFormat.getNumberInstance(Locale.US).format(transactionsPerSecond));
    }

}
