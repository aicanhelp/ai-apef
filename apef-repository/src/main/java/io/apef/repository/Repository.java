package io.apef.repository;

import io.apef.core.channel.BusinessChannel;
import io.apef.base.cache.CacheStats;
import io.apef.repository.channel.RepositoryChannelPipe;

public interface Repository<K, V> {
    RepositoryChannelPipe<K, V> repositoryChannelPipe(BusinessChannel srcChannel);

    CacheStats cacheStats();
}
