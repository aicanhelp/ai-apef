package io.apef.repository.redis.lettuce;

import io.apef.base.utils.KeyValueMerger;
import io.apef.repository.RepositoryConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class RedisRepositoryConfig extends RepositoryConfig {
    private int expireSecs = -1;
    private int expireOffsetRange = 900;
    private String tableName = null;
    private KeyValueMerger<?, ?> keyValueMerger = (key, value) -> {
    };

    @Override
    public RedisRepositoryConfig setName(String name) {
        super.setName(name);
        return this;
    }
}
