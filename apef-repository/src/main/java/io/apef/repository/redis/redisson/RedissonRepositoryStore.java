package io.apef.repository.redis.redisson;

import com.codahale.metrics.Timer;
import io.apef.base.utils.Expiration;
import io.apef.base.utils.KeyMapper;
import io.apef.base.utils.KeyValueMerger;
import io.apef.metrics.ApefMetric;
import io.apef.metrics.ApefMetricsFactory;
import io.apef.repository.channel.RepositoryChannelStore;
import io.apef.metrics.Metricable;
import io.apef.metrics.item.MetricItemTimer;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBatch;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static io.apef.base.utils.Bytes.stringOf;

@Slf4j
public class RedissonRepositoryStore<K, V> implements RepositoryChannelStore<K, V>, Metricable {
    private String tableName;
    private RedissonClient redissonClient;
    private RedissonCodec<K, V> redissonCodec;
    private KeyMapper<K, V> keyMapper;
    private KeyValueMerger<K, V> keyValueMerger;
    private Codec codec;
    private Expiration expiration;

    @Getter
    @Accessors(fluent = true)
    private ApefMetric metric;
    private MetricItemTimer getMetric, saveMetric, existMetric, getAllMetric, putAllMetric;

    private void initMetric() {
        this.metric = ApefMetricsFactory.defaultMetrics()
                .vexMetric("RedisRepositoryStore");
        this.getMetric = this.metric.timer("get");
        this.saveMetric = this.metric.timer("save");
        this.existMetric = this.metric.timer("exists");
        this.getAllMetric = this.metric.timer("getAll");
        this.putAllMetric = this.metric.timer("putAll");
    }

    @Override
    public void get(K key, RepositoryStoreHandler<V> handler) {
        Timer.Context timeStart = this.getMetric.start();
        if (this.tableName == null)
            redissonClient.getBucket(key(key), codec)
                    .getAsync().whenComplete((v, throwable) -> {
                if (timeStart != null) timeStart.stop();
                if (v != null)
                    keyValueMerger.merge(key, (V) v);
                handler.handle((V) v, throwable);
            });
        else {
            redissonClient.getMap(this.tableName)
                    .getAsync(key(key)).whenComplete((v, throwable) -> {
                if (timeStart != null) timeStart.stop();
                if (v != null)
                    keyValueMerger.merge(key, (V) v);
                handler.handle((V) v, throwable);
            });
        }
    }

    private String key(K key) {
        return stringOf(redissonCodec.encodeKey(key));
    }

    @Override
    public void save(V value, RepositoryStoreHandler<Boolean> handler) {
        Timer.Context timeStart = this.saveMetric.start();
        K key = this.keyMapper.keyOf(value);
        if (this.tableName == null)
            (this.expiration == null ? redissonClient.getBucket(key(key), codec)
                    .setAsync(value) :
                    redissonClient.getBucket(key(key), codec)
                            .setAsync(value, expiration.expirationSecs(), TimeUnit.SECONDS)
            ).whenComplete((v, throwable) -> {
                if (timeStart != null) timeStart.stop();
                if (throwable != null) {
                    handler.handle(false, throwable);
                } else {
                    handler.handle(true, throwable);
                }
            });
        else {
            redissonClient.getMap(this.tableName, codec)
                    .putAsync(key(key), value).whenComplete((v, throwable) -> {
                if (timeStart != null) timeStart.stop();
                if (throwable != null) {
                    handler.handle(false, throwable);
                } else {
                    handler.handle(true, throwable);
                }
            });
        }
    }

    @Override
    public void exists(K key, RepositoryStoreHandler<Boolean> handler) {
        Timer.Context timeStart = this.existMetric.start();
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
        Timer.Context timeStart = this.getAllMetric.start();
        if (this.tableName == null) {
            RBatch rBatch = redissonClient.createBatch();
            keys.forEach(k -> {
                rBatch.getBucket(key(k), codec).getAsync();
            });

            rBatch.executeAsync()
                    .whenComplete((vs, throwable) -> {
                        if (timeStart != null) timeStart.stop();
                        if (throwable == null) {
                            int index = 0;
                            for (K key : keys) {
                                V value = (V) (vs.get(index++));
                                if (value != null)
                                    keyValueMerger.merge(key, value);
                                values.put(key, value);
                            }
                            handler.handle(values, null);
                        } else {
                            handler.handle(null, new Exception("Error occurs on MGet for keys: " + keys));
                        }
                    });
        }
    }

    @Override
    public void putAll(Map<K, V> values, RepositoryStoreHandler<Boolean> handler) {
        Timer.Context timeStart = this.getAllMetric.start();
        if (this.tableName == null) {

            RBatch rBatch = redissonClient.createBatch();
            values.forEach((k, v) -> {
                if (this.expiration == null)
                    if (tableName == null) rBatch.getBucket(key(k), codec)
                            .setAsync(v);
                    else
                        rBatch.getMap(this.tableName, codec).putAsync(key(k), v);
                else
                    rBatch.getBucket(key(k), codec)
                            .setAsync(v, expiration.expirationSecs(), TimeUnit.SECONDS);
            });

            rBatch.executeAsync()
                    .whenComplete((vs, throwable) -> {
                        if (timeStart != null) timeStart.stop();
                        if (throwable != null) {
                            handler.handle(false, throwable);
                        } else {
                            handler.handle(true, throwable);
                        }
                    });
        }
    }
}
