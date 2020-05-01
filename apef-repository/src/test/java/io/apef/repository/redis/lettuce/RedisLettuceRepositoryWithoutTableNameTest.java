package io.apef.repository.redis.lettuce;

import org.testng.annotations.Test;

@Test
public class RedisLettuceRepositoryWithoutTableNameTest extends AbstractRedisRepositoryTest {

    public void testSaveExistsGet() {
        doTestSaveExistsGet(testContextBuilder("testSaveExistsGet").build());
    }

    public void testPutAllGetAll() {
        doTestPutAllGetAll(testContextBuilder("testPutAllGetAll").build());
    }
}