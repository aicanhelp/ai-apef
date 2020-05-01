package io.apef.base.cache;

public interface CacheStats {
    double hitRate();

    long hitCount();

    long missCount();

    void hit();

    void miss();

    void hit(int count);

    void miss(int count);

    void reset();
}
