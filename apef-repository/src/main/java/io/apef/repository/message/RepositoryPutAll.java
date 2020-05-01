package io.apef.repository.message;

import io.apef.core.channel.pipe.ChannelPipeContext;

import java.util.Map;

public interface RepositoryPutAll<M extends RepositoryPutAll<M, K, V>, K, V>
        extends RepositoryRequest<M, Map<K, V>, Boolean> {
    M values(Map<K, V> values);

    static <M extends RepositoryPutAll<M, K, V>, K, V> RepositoryPutAll<M, K, V> create(ChannelPipeContext channelPipeContext) {
        return new RepositoryPutAllMessage(channelPipeContext);
    }
}

