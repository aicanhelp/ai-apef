package io.apef.repository.redis.lettuce;

import com.codahale.metrics.Timer;
import io.apef.base.utils.Expiration;
import io.apef.base.utils.KeyMapper;
import io.apef.base.utils.KeyValueMerger;
import io.apef.metrics.ApefMetric;
import io.apef.repository.channel.RepositoryChannelStore;
import io.apef.repository.utils.RepositoryMetricsContext;
import io.apef.metrics.Metricable;
import com.google.common.base.Preconditions;
import com.lambdaworks.redis.AbstractRedisAsyncCommands;
import com.lambdaworks.redis.RedisFuture;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class RedisLettuceStoreBuilder<K, V> {
    @Setter
    @Accessors(fluent = true)
    protected String name;
    @Setter
    @Accessors(fluent = true)
    protected Expiration expiration;
    @Setter
    @Accessors(fluent = true)
    protected K tableName;
    @Setter
    @Accessors(fluent = true)
    protected KeyMapper<K, V> keyMapper;
    @Setter
    @Accessors(fluent = true)
    protected KeyValueMerger<K, V> keyValueMerger;

    protected AbstractRedisAsyncCommands<K, V> asyncCommands;
    protected RepositoryMetricsContext metricsContext;
    protected RedisLettuceRepositoryStore<K, V> redisLettuceStore;


    protected RedisLettuceStoreBuilder(
            AbstractRedisAsyncCommands<K, V> asyncCommands) {
        this.asyncCommands = asyncCommands;
    }

    public RedisLettuceRepositoryStore<K, V> build() {
        Preconditions.checkArgument(this.keyMapper != null, "KeyMapper is required");
        Preconditions.checkArgument(this.keyValueMerger != null, "KeyValueMerger is required");
        Preconditions.checkArgument(this.asyncCommands != null, "AsyncCommands is required");

        this.redisLettuceStore.metricsContext = new RepositoryMetricsContext(this.name);
        return redisLettuceStore;
    }
}


@Slf4j
class RedisLettuceRepositoryStore<K, V> extends RedisLettuceStoreBuilder<K, V>
        implements RepositoryChannelStore<K, V>, Metricable {
    @Override
    public ApefMetric metric() {
        return this.metricsContext.metric();
    }

    protected RedisLettuceRepositoryStore(AbstractRedisAsyncCommands<K, V> asyncCommands) {
        super(asyncCommands);
    }

    public static <K, V> RedisLettuceStoreBuilder<K, V> builder(AbstractRedisAsyncCommands<K, V> asyncCommands) {
        RedisLettuceRepositoryStore<K, V> builder = new RedisLettuceRepositoryStore<>(asyncCommands);
        builder.redisLettuceStore = builder;
        return builder;
    }

    @Override
    public void get(K key, RepositoryStoreHandler<V> handler) {
        Timer.Context timeStart = this.metricsContext.getMetric().start();
        RedisFuture<V> redisFuture;
        if (this.tableName == null)
            redisFuture = this.asyncCommands.get(key);
        else
            redisFuture = this.asyncCommands.hget(this.tableName, key);
        try {
            redisFuture
                    .whenComplete((v, throwable) -> {
                        if (timeStart != null) timeStart.stop();
                        if (v != null)
                            keyValueMerger.merge(key, v);
                        handler.handle(v, throwable);
                    });
        } catch (Exception ex) {
            handler.handle(null, ex);
        }
    }

    @Override
    public void save(V value, RepositoryStoreHandler<Boolean> handler) {
        Timer.Context timeStart = this.metricsContext.saveMetric().start();
        K key = this.keyMapper.keyOf(value);

        try {
            if (this.tableName == null) {
                (this.expiration != null ?
                        this.asyncCommands.setex(key, this.expiration.expirationSecs(), value) :
                        this.asyncCommands.set(key, value))
                        .whenComplete((v, throwable) -> {
                            if (timeStart != null) timeStart.stop();
                            if (throwable != null) {
                                handler.handle(false, throwable);
                            } else {
                                handler.handle(true, throwable);
                            }
                        });
            } else {
                this.asyncCommands.hset(this.tableName, key, value)
                        .whenComplete((v, throwable) -> {
                            if (timeStart != null) timeStart.stop();
                            if (throwable != null) {
                                handler.handle(false, throwable);
                            } else {
                                handler.handle(true, throwable);
                            }
                        });
            }
        } catch (Exception ex) {
            handler.handle(null, ex);
        }
    }

    @Override
    public void exists(K key, RepositoryStoreHandler<Boolean> handler) {
        Timer.Context timeStart = this.metricsContext.existMetric().start();
        this.get(key, (returnValue, throwable) -> {
            if (timeStart != null) timeStart.stop();
            if (returnValue != null)
                keyValueMerger.merge(key, returnValue);
            if (throwable != null) {
                handler.handle(false, throwable);
            } else {
                handler.handle(true, throwable);
            }
        });
    }

    @Override
    public void getAll(Set<K> keys, RepositoryStoreHandler<Map<K, V>> handler) {
        Map<K, V> values = new HashMap<>();
        Timer.Context timeStart = this.metricsContext.getAllMetric().start();
        try {
            (this.tableName == null ?
                    this.asyncCommands.mget(keys) :
                    this.asyncCommands.hmget(this.tableName, (K[]) keys.toArray()))
                    .whenComplete((vs, throwable) -> {
                        if (timeStart != null) timeStart.stop();
                        if (throwable == null) {
                            int index = 0;
                            for (K key : keys) {
                                V value = vs.get(index++);
                                if (value != null)
                                    keyValueMerger.merge(key, value);
                                values.put(key, value);
                            }
                            handler.handle(values, null);
                        } else {
                            handler.handle(null, new Exception("Error occurs on MGet for keys: " + keys));
                        }
                    });
        } catch (Exception ex) {
            handler.handle(null, ex);
        }
    }

    @Override
    public void putAll(Map<K, V> values, RepositoryStoreHandler<Boolean> handler) {
        Timer.Context timeStart = this.metricsContext.putAllMetric().start();
        try {
            (this.tableName == null ? this.asyncCommands.mset(values) :
                    this.asyncCommands.hmset(this.tableName, values))
                    .whenComplete((s, throwable) -> {
                        if (timeStart != null) timeStart.stop();
                        if (throwable != null) {
                            handler.handle(false, throwable);
                        } else {
                            if (this.expiration != null && this.tableName == null) {
                                values.keySet().forEach(k -> {
                                    this.asyncCommands.expire(k, this.expiration.expirationSecs());
                                });
                            }
                            handler.handle(true, throwable);
                        }
                    });
        } catch (Exception ex) {
            handler.handle(null, ex);
        }
    }
}
