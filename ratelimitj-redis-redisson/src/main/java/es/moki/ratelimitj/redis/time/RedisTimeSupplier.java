package es.moki.ratelimitj.redis.time;


import java.util.Iterator;
import java.util.concurrent.CompletionStage;

import javax.annotation.concurrent.ThreadSafe;

import es.moki.ratelimitj.core.time.TimeSupplier;
import org.redisson.api.Node;
import org.redisson.api.RedissonReactiveClient;
import reactor.core.publisher.Mono;

import static java.util.Objects.requireNonNull;

/**
 * A Redis based time supplier.
 * <p>It may be desirable to use a Redis based time supplier if the software is running on a group of servers with
 * different clocks or unreliable clocks. A disadvantage of the Redis based time supplier is that it introduces
 * an additional network round trip.
 */
@ThreadSafe
public class RedisTimeSupplier implements TimeSupplier {

    private final RedissonReactiveClient client;

    public RedisTimeSupplier(RedissonReactiveClient client) {
        this.client = requireNonNull(client);
    }

    //TODO currently just have reactive client implemented.
    @Deprecated
    @Override
    public CompletionStage<Long> getAsync() {
        throw new UnsupportedOperationException("Currently on reactive client is implemented");
    }

    //TODO need to investigate reactive programming to see the proper way of doing this
    @Override
    public Mono<Long> getReactive() {
        return Mono.just(getTime());
    }

    private long getTime()
    {
        Iterator<Node> iter = client.getNodesGroup().getNodes().iterator();
        Node node1 = iter.next();
        return Long.valueOf(node1.time().getSeconds());
    }

    @Override
    public long get() {
        return getTime();
    }
}
