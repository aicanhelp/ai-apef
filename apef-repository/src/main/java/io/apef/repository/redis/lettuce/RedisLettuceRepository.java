package io.apef.repository.redis.lettuce;

import io.apef.base.utils.Expiration;
import io.apef.base.utils.KeyMapper;
import io.apef.base.utils.KeyValueMerger;
import io.apef.repository.AbstractRepository;
import com.lambdaworks.redis.AbstractRedisAsyncCommands;

public class RedisLettuceRepository<K, V> extends AbstractRepository<K, V> {
    public RedisLettuceRepository(RedisRepositoryConfig repositoryConfig,
                                  AbstractRedisAsyncCommands<K, V> asyncCommands) {
        super(repositoryConfig.validate(), RedisLettuceRepositoryStore.builder(asyncCommands)
                .name(repositoryConfig.getName())
                .tableName((K) repositoryConfig.getTableName())
                .expiration(new Expiration(repositoryConfig.getExpireSecs(), repositoryConfig.getExpireOffsetRange()))
                .keyMapper((KeyMapper<K, V>) repositoryConfig.getKeyMapper())
                .keyValueMerger((KeyValueMerger<K, V>) repositoryConfig.getKeyValueMerger())
                .build());
    }

}
