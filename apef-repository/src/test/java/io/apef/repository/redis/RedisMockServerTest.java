package io.apef.repository.redis;

import io.apef.testing.unit.BaseUnitSpec;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Test
public class RedisMockServerTest extends BaseUnitSpec {

    public void testStartStopMockRedis() {
        RedisMockServer.startMockServer(25678);
        assertNotNull(RedisMockServer.redisProcessId(25678));
        RedisMockServer.stopMockServer(25678);
        assertTrue(StringUtils.isEmpty(RedisMockServer.redisProcessId(25678)));
    }
}