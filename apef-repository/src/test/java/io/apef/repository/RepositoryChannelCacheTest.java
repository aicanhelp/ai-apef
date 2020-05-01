package io.apef.repository;

import org.testng.annotations.Test;


public class RepositoryChannelCacheTest extends RepositoryChannelTestBase {
    @Test
    public void testCacheStats() {
        doTestCacheStats(testContextBuilder("testCacheStats").build());
    }
}
