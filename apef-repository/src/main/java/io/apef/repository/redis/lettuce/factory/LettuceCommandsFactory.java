package io.apef.repository.redis.lettuce.factory;

import com.lambdaworks.redis.AbstractRedisAsyncCommands;
import com.lambdaworks.redis.ClientOptions;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.cluster.ClusterClientOptions;
import com.lambdaworks.redis.cluster.ClusterTopologyRefreshOptions;
import com.lambdaworks.redis.cluster.RedisClusterClient;
import com.lambdaworks.redis.codec.RedisCodec;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LettuceCommandsFactory {
    private RedisConfig redisConfig;
    private RedisClient redisClient;
    private RedisClusterClient redisClusterClient;

    public LettuceCommandsFactory(RedisConfig redisConfig) {
        this.redisConfig = redisConfig;

        if (this.redisConfig.isCluster()) {
            this.buildRedisClusterClient(redisConfig);
        } else {
            this.buildRedisClient(redisConfig);
        }
    }

    private void buildRedisClient(RedisConfig redisConfig) {
        this.redisClient = RedisClient.create(redisConfig.getAddresses());
        ClientOptions.Builder clientOptionsBuilder = ClientOptions.builder();
        clientOptionsBuilder.autoReconnect(redisConfig.isAutoReconnect())
                .disconnectedBehavior(redisConfig.getDisconnectedBehavior());
        this.redisClient.setDefaultTimeout(redisConfig.getTimeoutSec(), TimeUnit.SECONDS);
        this.redisClient
                .setOptions(clientOptionsBuilder.build());
    }

    private void buildRedisClusterClient(RedisConfig redisConfig) {
        this.redisClusterClient = RedisClusterClient.create(
                buildRedisClusterAddresses(redisConfig.getAddresses())
        );
        ClusterClientOptions.Builder clientOptionsBuilder = ClusterClientOptions.builder()
                .topologyRefreshOptions(ClusterTopologyRefreshOptions
                        .builder()
                        .enablePeriodicRefresh(true)
                        .enablePeriodicRefresh(redisConfig.getClusterRefreshPeriodSec(), TimeUnit.SECONDS)
                        .build()
                )
                .autoReconnect(redisConfig.isAutoReconnect())
                .disconnectedBehavior(redisConfig.getDisconnectedBehavior());
        this.redisClusterClient.setDefaultTimeout(redisConfig.getTimeoutSec(), TimeUnit.SECONDS);
        this.redisClusterClient.setOptions(clientOptionsBuilder.build());
    }

    public void close() {
        this.redisClient.shutdown();
    }

    private static List<RedisURI> buildRedisClusterAddresses(String redisAddress) {
        List<RedisURI> redisURIs = new ArrayList<>();
        char splitter = ',';
        if (redisAddress.contains(";")) {
            splitter = ';';
        } else if (redisAddress.contains(" ")) {
            splitter = ' ';
        }

        for (String redisUri : StringUtils.split(redisAddress, splitter)) {
            if (!StringUtils.isEmpty(redisUri)) {
                redisURIs.add(RedisURI.create(redisUri));
            }
        }
        return redisURIs;
    }

    public <K, V> AbstractRedisAsyncCommands<K, V> asyncCommands(RedisCodec<K, V> redisCodec) {
        if (this.redisClient != null) {
            this.redisClient.connectPubSub().async();
            return (AbstractRedisAsyncCommands<K, V>) this.redisClient.connect(redisCodec).async();
        }
        if (this.redisClusterClient != null) {
            return (AbstractRedisAsyncCommands<K, V>) this.redisClusterClient.connect(redisCodec).async();
        }
        return null;
    }

    public <K, V> AbstractRedisAsyncCommands<K, V> asyncPubSubCommands(RedisCodec<K, V> redisCodec) {
        if (this.redisClient != null) {
            return (AbstractRedisAsyncCommands<K, V>) this.redisClient.connectPubSub(redisCodec).async();
        }
        if (this.redisClusterClient != null) {
            return (AbstractRedisAsyncCommands<K, V>) this.redisClusterClient.connectPubSub(redisCodec).async();
        }
        return null;
    }

}
