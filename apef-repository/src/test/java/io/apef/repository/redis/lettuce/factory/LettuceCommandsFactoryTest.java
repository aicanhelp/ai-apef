package io.apef.repository.redis.lettuce.factory;

import com.lambdaworks.redis.codec.ByteArrayCodec;
import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.util.Random;

import static org.testng.Assert.*;

@Test(enabled = false)
public class LettuceCommandsFactoryTest extends BaseUnitSpec {
    int port = 30000 + new Random().nextInt(30000);
    RedisServer redisServer;

    @BeforeClass
    public void setup() throws IOException {
        redisServer = new RedisServer(port);
        redisServer.start();
    }

    @AfterClass
    public void teardown() {
        redisServer.stop();
    }


    public void testAsyncCommandsFactory() {
        LettuceCommandsFactory commandsFactory =
                new LettuceCommandsFactory(new RedisConfig().setAddresses("redis://localhost:" + port));

        assertNotNull(commandsFactory.asyncCommands(new ByteArrayCodec()));
        assertNotNull(commandsFactory.asyncPubSubCommands(new ByteArrayCodec()));
    }
}