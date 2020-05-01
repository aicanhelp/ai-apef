package io.apef.repository.lrucache;

import io.apef.core.channel.BusinessChannel;
import io.apef.core.channel.BusinessChannelImpl;
import io.apef.core.channel.ChannelConfig;
import io.apef.repository.RepositoryConfig;
import io.apef.repository.RepositoryTestBase;
import io.apef.repository.channel.RepositoryChannelPipe;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Random;

import static io.apef.testing.benchmark.Benchmark.benchmark;

public class LRUCacheRepositoryBenchmark implements RepositoryTestBase {
    protected RepositoryChannelPipe<String, TestObject> repositoryChannelPipe;

    @BeforeClass
    public void setup() {
        BusinessChannel sourceChannel = new BusinessChannelImpl(new ChannelConfig()
                .setName("test").setQueueSize(8092));

        sourceChannel.start();
        RepositoryConfig repositoryConfig = new RepositoryConfig()
                .setEnableCache(true).setEnableCacheMetrics(true)
                .setKeyMapper(request -> ((TestObject) request).id());
        repositoryConfig.setName("RepositoryChannel" + new Random().nextInt());
        LRUCacheRepository<String, TestObject> repository =
                new LRUCacheRepository<>(repositoryConfig);
        repositoryChannelPipe = repository.repositoryChannelPipe(sourceChannel);
    }

    //~ 700M tps/thread
    @Test
    public void benchmarkSave() {
        benchmarkSave(repositoryChannelPipe, 10000);
    }

    //~ 1000M tps/thread
    @Test
    public void benchmarkGet() {
        benchmarkGet(repositoryChannelPipe, 10000);
    }

    //~ 2400M tps/thread
    @Test
    public void benchmarkGet2() {
        benchmark().threads(1).concurrency(1000)
                .iterations(10000)
                .rounds(10)
                .reportInterval(5)
                .async(false)
                .benchmarkTask((index, runnerContext) -> {
                    repositoryChannelPipe.get()
                            .key("key_" + index)
                            .onFailure((errMsg, cause) -> {
                                log.error(errMsg, cause);
                                runnerContext.done(index);
                            })
                            .onSuccess((responseContent) -> {
                                runnerContext.done(index);
                            })
                            .end();
                }).start();
    }

}
