package io.apef.repository.message;

import io.apef.core.channel.pipe.ChannelPipeContext;

public interface RepositoryExists<M extends RepositoryExists<M, K>, K>
        extends RepositoryRequest<M, K, Boolean> {
    M key(K key);
    /**
     * true: If not in cache, read from repository store
     * @param readThrough
     * @return
     */
    M readThrough(boolean readThrough);

    static <M extends RepositoryExists<M, K>, K>
    RepositoryExists<M, K> create(ChannelPipeContext channelPipeContext) {
        return new RepositoryExistsMessage(channelPipeContext);
    }
}

