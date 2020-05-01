package io.apef.repository.redis.lettuce;

import org.testng.annotations.Test;

public class RedisLettuceRepositoryCacheTest extends AbstractRedisRepositoryTest {
    @Test
    public void testCacheStats() {
        doTestCacheStats(testContextBuilder("testCacheStats").build());
    }
}
