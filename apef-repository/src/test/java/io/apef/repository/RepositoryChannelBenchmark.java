package io.apef.repository;

import org.testng.annotations.Test;

public class RepositoryChannelBenchmark extends RepositoryChannelTestBase {

    //~300M
    @Test
    public void benchmarkSave() {
        TestContext testContext = testContextBuilder("benchmarkSave")
                .metrics(false)
                .build();
        benchmarkSave(testContext.repositoryChannelPipe(), 2000);
        testContext.close();
    }

    //~500M without metric
    @Test
    public void benchmarkGet() {
        TestContext testContext = testContextBuilder("benchmarkGet")
                .metrics(false)
                .build();
        benchmarkGet(testContext.repositoryChannelPipe(), 2000);
        testContext.close();
    }

    //~400M
    @Test
    public void benchmarkSave2() {
        TestContext testContext = testContextBuilder("benchmarkSave2")
                .cacheOnly(true)
                .metrics(false)
                .build();
        benchmarkSave(testContext.repositoryChannelPipe(), 2000);
        testContext.close();
    }

    //~600M without metric
    @Test
    public void benchmarkGet2() {
        TestContext testContext = testContextBuilder("benchmarkGet2")
                .cacheOnly(true)
                .metrics(false)
                .build();
        benchmarkGet(testContext.repositoryChannelPipe(), 2000);
        testContext.close();
    }
}
