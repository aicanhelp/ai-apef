package io.apef.repository.channel;

import io.apef.core.channel.BusinessChannel;
import io.apef.core.channel.impl.FromBChannelPipeIml;
import io.apef.repository.message.*;

public class RepositoryChannelPipeImpl<K, V> extends FromBChannelPipeIml
        implements RepositoryChannelPipe<K, V> {
    private RepositoryChannelPipeInterceptor interceptor;

    public RepositoryChannelPipeImpl(BusinessChannel srcChannel,
                                     RepositoryChannel destChannel,
                                     RepositoryChannelPipeInterceptor interceptor) {
        super(srcChannel, destChannel, interceptor);
        this.interceptor = interceptor;
    }

    public RepositoryChannelPipeImpl(BusinessChannel srcChannel,
                                     RepositoryChannel destChannel) {
        super(srcChannel, destChannel, new RepositoryChannelPipeInterceptor(destChannel.repositoryCache(), false));
    }

    public RepositoryChannelPipeImpl(BusinessChannel srcChannel,
                                     RepositoryChannel destChannel, boolean cacheOnly) {
        super(srcChannel, destChannel, new RepositoryChannelPipeInterceptor(destChannel.repositoryCache(), cacheOnly));
    }

    @Override
    public void cacheOnly(boolean cacheOnly) {
        this.interceptor.setCacheOnly(cacheOnly);
    }

    public <M extends RepositoryGet<M, K, V>> RepositoryGet<M, K, V> get() {
        return RepositoryGet.create(this);
    }

    public <M extends RepositorySave<M, V>> RepositorySave<M, V> save() {
        return RepositorySave.create(this);
    }

    public <M extends RepositoryExists<M, K>> RepositoryExists<M, K> exists() {
        return RepositoryExists.create(this);
    }

    public <M extends RepositoryGetAll<M, K, V>> RepositoryGetAll<M, K, V> getAll() {
        return RepositoryGetAll.create(this);
    }

    public <M extends RepositoryPutAll<M, K, V>> RepositoryPutAll<M, K, V> putAll() {
        return RepositoryPutAll.create(this);
    }
}
