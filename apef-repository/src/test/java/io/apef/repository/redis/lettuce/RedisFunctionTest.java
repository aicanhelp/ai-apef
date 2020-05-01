package io.apef.repository.redis.lettuce;

import io.apef.testing.unit.BaseUnitSpec;
import io.lettuce.core.*;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;

public class RedisFunctionTest extends AbstractRedisRepositoryTest {
    @Test
    public void testExpireValue() {
        Blocker blocker1 = new Blocker();
        asyncCommands.setex("key_1", -1, new TestObject("key_1", null))
                .whenComplete((s, throwable) -> {
                    blocker1.assertNotNull(throwable).end();
                });
        blocker1.awaitEnd();

        Blocker blocker2 = new Blocker();
        asyncCommands.setex("key_1", 0, new TestObject("key_1", null))
                .whenComplete((s, throwable) -> {
                    blocker2.assertNotNull(throwable).end();
                });
        blocker2.awaitSec(1);
        blocker2.awaitEnd();
    }

//    @Test
    public void testPartitions() {
        RedisAdvancedClusterAsyncCommands<String, String> asyncCommands = this.asyncCommands2();
        long startTime = System.currentTimeMillis();
        int index = 0;
        while (true) {
            asyncCommands.set("{key_1}" + (index++), "value")
                    .whenComplete((s, throwable) -> {

                    });
            blockingMs(1);
            if (System.currentTimeMillis() - startTime > 60000) break;
        }
        asyncCommands.shutdown(true);
    }

    private RedisAdvancedClusterAsyncCommands<String, String> asyncCommands2() {
        List<RedisURI> addresses = new ArrayList<>();

        for (int i = 7001; i < 7004; i++) {
            addresses.add(RedisURI.create("redis://localhost:" + i));
        }

        RedisClusterClient redisClient = RedisClusterClient.create(addresses);

        ClusterClientOptions.Builder builder = ClusterClientOptions.builder();
        builder.socketOptions(SocketOptions.builder()
                .connectTimeout(1, TimeUnit.SECONDS)
                .keepAlive(true)
                .build());
        redisClient.setDefaultTimeout(1, TimeUnit.SECONDS);
        redisClient
                .setOptions(builder
                        .autoReconnect(false)
                        .topologyRefreshOptions(
                                ClusterTopologyRefreshOptions.builder()
                                        .closeStaleConnections(true)
                                        .refreshTriggersReconnectAttempts(1)
                                        .enableAllAdaptiveRefreshTriggers()
                                        .enablePeriodicRefresh(3, TimeUnit.SECONDS)
                                        .build()
                        )
                        .cancelCommandsOnReconnectFailure(true)
                        .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                        .build());
        return redisClient.connect().async();
    }

//    @Test
    public void test() {

        RedisAdvancedClusterAsyncCommands<String, String> asyncCommands = this.asyncCommands2();

        asyncCommands.setTimeout(1, TimeUnit.SECONDS);
        AtomicInteger counter1 = new AtomicInteger();
        AtomicInteger counter2 = new AtomicInteger();
        AtomicInteger successCounter = new AtomicInteger();
        AtomicInteger errorCounter1 = new AtomicInteger();
        AtomicInteger errorCounter2 = new AtomicInteger();
        AtomicInteger errorCounter3 = new AtomicInteger();
        long startTime = System.currentTimeMillis();
        int index = 0;
        long lastSpentTime = 0;
        while (true) {
            counter1.incrementAndGet();
            try {
                String key = "" + index++;

//                asyncCommands.setex(key, 10, "value")
                asyncCommands.get(key)
                        .whenComplete((s, throwable) -> {
                            counter2.incrementAndGet();
                            if (throwable != null) {
                                errorCounter1.incrementAndGet();
                            } else {
                                successCounter.incrementAndGet();
                            }
                        });
                if (index > 100000) index = 0;
            } catch (Throwable ex) {
                counter2.incrementAndGet();
                if (ex instanceof RedisException)
                    errorCounter2.incrementAndGet();
                else
                    errorCounter3.incrementAndGet();
            }
            blockingMs(1);
            long spentTime = System.currentTimeMillis() - startTime;

            if (spentTime - lastSpentTime > 1000) {
                lastSpentTime = spentTime;
                BaseUnitSpec.log.info("Responsed: {}, Success: {}, Failure: {}, Err1: {}, Err2: {}",
                        counter2.get(), successCounter.get(), errorCounter1.get(), errorCounter2.get(), errorCounter3.get());
            }
            if (spentTime > 30000) break;
        }

        blockingSec(10);

        BaseUnitSpec.log.info("Sent: {}, Responsed: {}, Success: {}, Failure: {}, Err1: {}, Err2: {}",
                counter1.get(), counter2.get(), successCounter.get(), errorCounter1.get(), errorCounter2.get(), errorCounter3.get());
    }

//    @Test
    public void testRedisson() {
        Config config = new Config();

        config.useClusterServers()
                .setScanInterval(2000)
                .addNodeAddress("localhost:7000", "localhost:7001")
                .addNodeAddress("localhost:7002", "localhost:7004");

        RedissonClient redisson = Redisson.create(config);

        Blocker blocker = new Blocker();

        redisson.getBucket("key1")
                .setAsync("value")
                .whenComplete((aVoid, throwable) -> {
                    redisson.getBucket("key1")
                            .getAsync()
                            .whenComplete((o, throwable1) -> {
                                blocker.assertEquals(o, "value").end();
                            });
                });
        blocker.awaitEnd();

        AtomicInteger counter1 = new AtomicInteger();
        AtomicInteger counter2 = new AtomicInteger();
        AtomicInteger successCounter = new AtomicInteger();
        AtomicInteger errorCounter1 = new AtomicInteger();
        AtomicInteger errorCounter2 = new AtomicInteger();
        long startTime = System.currentTimeMillis();
        int index = 0;
        long lastSpentTime = 0;
        while (true) {
            counter1.incrementAndGet();

            try {
                String key = "" + index++;

                redisson.getBucket(key)
                        .setAsync("value")
                        .whenComplete((s, throwable) -> {
                            counter2.incrementAndGet();
                            if (throwable != null) {
                                errorCounter1.incrementAndGet();
                            } else {
                                successCounter.incrementAndGet();
                            }
                        });
                if (index > 100000) index = 0;
            } catch (Throwable ex) {
                counter2.incrementAndGet();
                errorCounter2.incrementAndGet();
            }
            blockingMs(1);
            long spentTime = System.currentTimeMillis() - startTime;

            if (spentTime - lastSpentTime > 1000) {
                lastSpentTime = spentTime;
                BaseUnitSpec.log.info("Responsed: {}, Success: {}, Failure: {}, Reject: {}",
                        counter2.get(), successCounter.get(), errorCounter1.get(), errorCounter2.get());
            }
            if (spentTime > 30000) break;
        }

        blockingSec(10);

        BaseUnitSpec.log.info("Sent:{}, accepted: {}",
                counter1.get(), counter2.get());

    }
}
