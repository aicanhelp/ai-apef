package io.apef.repository.channel;

import io.apef.core.channel.pipe.B2CPipe;
import io.apef.repository.message.*;

public interface RepositoryChannelPipe<K, V> extends B2CPipe {
    <M extends RepositoryGet<M, K, V>> RepositoryGet<M, K, V> get();

    <M extends RepositorySave<M, V>> RepositorySave<M, V> save();

    <M extends RepositoryExists<M, K>> RepositoryExists<M, K> exists();

    <M extends RepositoryGetAll<M, K, V>> RepositoryGetAll<M, K, V> getAll();

    <M extends RepositoryPutAll<M, K, V>> RepositoryPutAll<M, K, V> putAll();

    void cacheOnly(boolean cacheOnly);
}
