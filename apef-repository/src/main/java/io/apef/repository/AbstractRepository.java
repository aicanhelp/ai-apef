package io.apef.repository;


import io.apef.core.channel.BusinessChannel;
import io.apef.base.cache.CacheStats;
import io.apef.repository.channel.*;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public abstract class AbstractRepository<K, V> implements Repository<K, V> {
    private RepositoryChannel<K, V> repositoryChannel;
    private RepositoryCache<K, V> repositoryCache;
    private RepositoryChannelStore<K, V> repositoryStore;

    protected AbstractRepository() {

    }

    protected void buildWith(RepositoryConfig channelConfig,
                             RepositoryChannelStore<K, V> repositoryStore) {
        if (channelConfig.isEnableCache()) {
            this.repositoryCache = new LRURepositoryCache<>(channelConfig);
        }
        this.repositoryStore = repositoryStore;
        this.repositoryChannel = new RepositoryChannel<>(channelConfig,
                this.repositoryCache, repositoryStore);
    }

    protected AbstractRepository(RepositoryConfig channelConfig,
                                 RepositoryChannelStore<K, V> repositoryStore) {
        buildWith(channelConfig, repositoryStore);
    }

    public RepositoryChannelPipe<K, V> repositoryChannelPipe(BusinessChannel srcChannel) {
        return this.repositoryChannel.repositoryChannelPipe(srcChannel);
    }

    public CacheStats cacheStats() {
        if (this.repositoryCache == null) return null;
        return this.repositoryCache.stats();
    }
}
