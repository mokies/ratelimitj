package es.moki.ratelimitj.core.time;

import java.util.concurrent.CompletionStage;


public interface TimeSupplier {

    CompletionStage<Long> getAsync();

    long get();
}
