package io.apef.repository.lrucache;

import io.apef.core.channel.BusinessChannel;
import io.apef.base.cache.LRUCache;
import io.apef.repository.AbstractRepository;
import io.apef.repository.RepositoryConfig;
import io.apef.repository.channel.RepositoryChannelPipe;

public class LRUCacheRepository<K, V> extends AbstractRepository<K, V> {
    private RepositoryConfig channelConfig;
    private LRUCache<K, V> lruCache;

    public LRUCacheRepository(RepositoryConfig channelConfig) {
        super(channelConfig.setEnableCache(false)
                .setCacheOnly(false), null);

        this.channelConfig = channelConfig;
        this.lruCache = new LRUCache<K, V>(channelConfig.getMaxCachedSize());
    }

    public RepositoryChannelPipe<K, V> repositoryChannelPipe(BusinessChannel srcChannel) {
        return new LRUCacheRepositoryChannelPipeImpl<K, V>(srcChannel,
                null, this.lruCache, this.channelConfig.getKeyMapper());
    }
}
