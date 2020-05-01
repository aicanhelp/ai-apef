package io.apef.repository.message;

import io.apef.core.channel.pipe.ChannelPipeContext;

public interface RepositoryGet<M extends RepositoryGet<M, K, V>, K, V>
        extends RepositoryRequest<M, K, V> {
    M key(K key);

    /**
     * true: If not in cache, read from repository store
     * @param readThrough
     * @return
     */
    M readThrough(boolean readThrough);

    static <M extends RepositoryGet<M, K, V>, K, V> RepositoryGet<M, K, V>
    create(ChannelPipeContext channelPipeContext) {
        return new RepositoryGetMessage(channelPipeContext);
    }
}

