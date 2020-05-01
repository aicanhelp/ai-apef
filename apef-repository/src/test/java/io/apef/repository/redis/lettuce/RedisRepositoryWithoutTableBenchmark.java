package io.apef.repository.redis.lettuce;


import org.testng.annotations.Test;

public class RedisRepositoryWithoutTableBenchmark extends AbstractRedisRepositoryTest {

    @Test
    public void benchmarkSave() {
        TestContext testContext = testContextBuilder("benchmarkSave").build();
        benchmarkSave(testContext.repositoryChannelPipe(), 1000);
        testContext.close();
    }

    @Test
    public void benchmarkGet() {
        TestContext testContext = testContextBuilder("benchmarkGet").build();
        benchmarkGet(testContext.repositoryChannelPipe(), 1000);
        testContext.close();
    }

    @Test
    public void benchmarkGetWithTimeout() {
        TestContext testContext = testContextBuilder("benchmarkGet").build();
        benchmarkGet(testContext.repositoryChannelPipe(), 1000, 1000);
        testContext.close();
    }

    @Test
    public void benchmarkSaveAndGet() {
        TestContext testContext = testContextBuilder("benchmarkSaveAndGet")
                .maxCacheSize(1000000)
                .build();
        benchmarkSaveAndGet(testContext.repositoryChannelPipe(), 1000);
        testContext.close();
    }

    @Test
    public void benchmarkSaveCacheOnly() {
        TestContext testContext = testContextBuilder("benchmarkSaveCacheOnly")
                .cacheOnly(true)
                .build();
        benchmarkSave(testContext.repositoryChannelPipe(), 2000);
        testContext.close();
    }

    @Override
    protected String redisAddress() {
        return "redis://localhost:" + 7004;
    }

    @Override
    protected boolean isCluster() {
        return true;
    }
}
