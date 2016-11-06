package es.moki.ratelimitj.internal.test;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import es.moki.ratelimitj.core.api.LimitRule;
import es.moki.ratelimitj.core.api.RateLimiter;
import es.moki.ratelimitj.core.time.TimeBanditSupplier;
import es.moki.ratelimitj.core.time.TimeSupplier;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;


public abstract class AbstractSyncRateLimiterPerformanceTest {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final TimeBanditSupplier timeBandit = new TimeBanditSupplier();

    protected abstract RateLimiter getRateLimiter(Set<LimitRule> rules, TimeSupplier timeSupplier);

    @Test
    public void shouldLimitDualWindowSyncTimed() throws Exception {

        Stopwatch watch = Stopwatch.createStarted();

        ImmutableSet<LimitRule> rules =
                ImmutableSet.of(LimitRule.of(2, TimeUnit.SECONDS, 100), LimitRule.of(10, TimeUnit.SECONDS, 100));
        RateLimiter rateLimiter = getRateLimiter(rules, timeBandit);
        Random rand = new Random();

        int total = 10_000;
        IntStream.rangeClosed(1, total).map(i -> rand.nextInt(128)).forEach(value -> {
            timeBandit.addUnixTimeMilliSeconds(200L);
            rateLimiter.overLimit("ip:127.0.0." + value);
        });

        double transactionsPerSecond = Math.ceil((double) total / watch.elapsed(TimeUnit.MILLISECONDS) * 1000);
        log.info("total time " + watch.stop()
                + " checks " + transactionsPerSecond + "/sec");
    }

}
