package io.apef.repository.redis.lettuce.factory;

import com.lambdaworks.redis.ClientOptions;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RedisConfig {
    private boolean cluster = false;
    private String addresses = "redis://localhost";
    private boolean autoReconnect = true;
    private int timeoutSec=1;
    private int requestQueueSize = 65536;
    private int clusterRefreshPeriodSec=5;
    private ClientOptions.DisconnectedBehavior disconnectedBehavior =
            ClientOptions.DisconnectedBehavior.REJECT_COMMANDS;
}
