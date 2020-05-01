package io.apef.repository.lrucache;

import io.apef.core.channel.BusinessChannel;
import io.apef.core.channel.Channel;
import io.apef.core.channel.impl.FromBChannelPipeIml;
import io.apef.base.cache.LRUCache;
import io.apef.base.utils.KeyMapper;
import io.apef.repository.channel.RepositoryChannelPipe;
import io.apef.repository.message.*;

public class LRUCacheRepositoryChannelPipeImpl<K, V> extends
        FromBChannelPipeIml implements RepositoryChannelPipe<K, V> {
    private LRUCache<K, V> lruCache;
    private KeyMapper<K, V> keyMapper;


    public LRUCacheRepositoryChannelPipeImpl(BusinessChannel srcChannel,
                                             Channel destChannel,
                                             LRUCache<K, V> lruCache,
                                             KeyMapper<K, V> keyMapper) {
        super(srcChannel, destChannel, null);
        this.lruCache = lruCache;
        this.keyMapper = keyMapper;
    }

    @Override
    public <M extends RepositoryGet<M, K, V>> RepositoryGet<M, K, V> get() {
        return new RepositoryGetMessage() {
            @Override
            public void end() {
                this.responseFuture().complete(lruCache.get(this.key()));
            }
        };
    }

    @Override
    public <M extends RepositorySave<M, V>> RepositorySave<M, V> save() {
        return new RepositorySaveMessage() {
            @Override
            public void end() {
                lruCache.put(keyMapper.keyOf((V) this.value()), (V) this.value());
                this.responseFuture().complete(true);
            }
        };
    }

    @Override
    public <M extends RepositoryExists<M, K>> RepositoryExists<M, K> exists() {
        return new RepositoryExistsMessage() {
            @Override
            public void end() {
                this.responseFuture().complete(lruCache.containsKey(this.key()));
            }
        };
    }

    @Override
    public <M extends RepositoryGetAll<M, K, V>> RepositoryGetAll<M, K, V> getAll() {
        return new RepositoryGetAllMessage() {
            @Override
            public void end() {
                this.responseFuture().complete(lruCache.getAll(this.keys()));
            }
        };
    }

    @Override
    public <M extends RepositoryPutAll<M, K, V>> RepositoryPutAll<M, K, V> putAll() {
        return new RepositoryPutAllMessage() {
            @Override
            public void end() {
                lruCache.putAll(this.values());
                this.responseFuture().complete(true);
            }
        };
    }

    @Override
    public void cacheOnly(boolean cacheOnly) {
        return;
    }
}
