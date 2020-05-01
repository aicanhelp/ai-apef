package io.apef.base.utils;

public interface KeyValueMerger<K, V> {
    void merge(K key, V value);
}
