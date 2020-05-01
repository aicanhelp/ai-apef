package io.apef.repository.redis.lettuce;

import org.testng.annotations.Test;

@Test
public class RedisLettuceRepositoryWithTableNameTest extends AbstractRedisRepositoryTest {

    public void testSaveExistsGet() {
        doTestSaveExistsGet(testContextBuilder("testSaveExistsGetTable")
                .tableName("table")
                .build());
    }

    public void testPutAllGetAll() {
        doTestPutAllGetAll(testContextBuilder("testPutAllGetAllTable")
                .tableName("table")
                .build());
    }

}