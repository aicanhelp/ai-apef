package io.apef.repository;

import org.testng.annotations.Test;


@Test(singleThreaded = true)
public class RepositoryChannelTest extends RepositoryChannelTestBase {

    public void testSaveExistsGet() {
        doTestSaveExistsGet(testContextBuilder("testSaveExistsGet").build());
    }

    public void testPutAllGetAll() {
        doTestPutAllGetAll(testContextBuilder("testPutAllGetAll").build());
    }

    public void testReadThrough() {
        doTestReadThrough(testContextBuilder("testReadThrough").build());
    }

    public void testCacheOnly() {
        doTestCacheOnly(testContextBuilder("testCacheOnly")
                .cacheOnly(true)
                .build());
    }
}
