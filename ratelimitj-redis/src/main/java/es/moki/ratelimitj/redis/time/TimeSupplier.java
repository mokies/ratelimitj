package es.moki.ratelimitj.redis.time;

import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

public interface TimeSupplier extends Supplier<CompletionStage<Long>> {
}
