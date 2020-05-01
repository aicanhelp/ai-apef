package io.apef.base.utils;


public interface KeyMapper<K, V> {
    K keyOf(V object);
}
