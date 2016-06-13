package es.moki.ratelimitj.hazelcast;


import org.junit.Test;

public class HazelcastSlidingWindowTest {

    @Test
    public void shouldCalCeil() {
        long blocks = (long) Math.ceil(System.currentTimeMillis() / 20);
        System.out.println(blocks);
    }
}