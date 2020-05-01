package io.apef.base.cache;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * UnThreadSafe Implementation
 */
@Getter
@Accessors(fluent = true)
public class CacheStatsUnThreadSafeImpl implements CacheStats {
    private long hitCount;
    private long missCount;

    @Override
    public double hitRate() {
        long requestCount = this.hitCount + this.missCount;
        return requestCount == 0L ? 1.0D : (double) this.hitCount / (double) requestCount;
    }

    @Override
    public void hit() {
        this.hitCount = this.hitCount + 1;
    }

    @Override
    public void miss() {
        this.missCount = this.missCount + 1;
    }

    @Override
    public void hit(int count) {
        this.hitCount = this.hitCount + count;
    }

    @Override
    public void miss(int count) {
        this.missCount = this.missCount + count;
    }

    @Override
    public void reset() {
        this.hitCount = 0;
        this.missCount = 0;
    }
}
