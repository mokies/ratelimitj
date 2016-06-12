package es.moki.ratelimitj;

import java.util.concurrent.CompletionStage;

public interface RateLimit {

    CompletionStage<Boolean> overLimit(String key);

    CompletionStage<Boolean> overLimit(String key, int weight);
}
