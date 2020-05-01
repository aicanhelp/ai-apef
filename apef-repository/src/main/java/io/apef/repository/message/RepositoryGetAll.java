package io.apef.repository.message;

import io.apef.core.channel.pipe.ChannelPipeContext;

import java.util.Map;
import java.util.Set;

public interface RepositoryGetAll<M extends RepositoryGetAll<M, K, V>, K, V>
        extends RepositoryRequest<M, Set<K>, Map<K, V>> {
    M keys(Set<K> key);
    /**
     * true: If not in cache, read from repository store
     * @param readThrough
     * @return
     */
    M readThrough(boolean readThrough);

    static <M extends RepositoryGetAll<M, K, V>, K, V> RepositoryGetAll<M, K, V> create(ChannelPipeContext channelPipeContext) {
        return new RepositoryGetAllMessage(channelPipeContext);
    }
}

