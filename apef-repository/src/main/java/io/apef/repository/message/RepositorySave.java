package io.apef.repository.message;

import io.apef.core.channel.pipe.ChannelPipeContext;

public interface RepositorySave<M extends RepositorySave<M, V>, V>
        extends RepositoryRequest<M, V, Boolean> {

    M value(V value);

    static <M extends RepositorySave<M, V>, V> RepositorySave<M, V> create(ChannelPipeContext channelPipeContext) {
        return new RepositorySaveMessage(channelPipeContext);
    }
}

