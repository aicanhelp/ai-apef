package io.apef.repository.channel;

import java.util.Map;
import java.util.Set;

public interface RepositoryChannelStore<K, V> {
    void get(K key, RepositoryStoreHandler<V> handler);

    void save(V value, RepositoryStoreHandler<Boolean> handler);

    void exists(K key, RepositoryStoreHandler<Boolean> handler);

    void getAll(Set<K> keys, RepositoryStoreHandler<Map<K, V>> handler);

    void putAll(Map<K, V> values, RepositoryStoreHandler<Boolean> handler);

    interface RepositoryStoreHandler<R> {
        void handle(R returnValue, Throwable ex);
    }
}
