package es.moki.ratelimitj.inmemory.request;

import java.util.OptionalInt;

public class SavedKey {

    public final long blockId;
    public final long blocks;
    public final long trimBefore;
    public final String countKey;
    public final String tsKey;

    public SavedKey(long now, int duration, OptionalInt precisionOpt) {

        int precision = precisionOpt.orElse(duration);
        precision = Math.min(precision, duration);

        this.blocks = (long) Math.ceil(duration / (double) precision);
        this.blockId = (long) Math.floor(now / (double) precision);
        this.trimBefore = blockId - blocks + 1;
        this.countKey = "" + duration + ':' + precision + ':';
        this.tsKey = countKey + 'o';
    }
}
