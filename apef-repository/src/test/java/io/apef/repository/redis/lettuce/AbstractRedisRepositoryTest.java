package io.apef.repository.redis.lettuce;

import io.apef.core.channel.BusinessChannelImpl;
import io.apef.core.channel.ChannelConfig;
import io.apef.repository.redis.lettuce.factory.LettuceCommandsFactory;
import io.apef.repository.redis.lettuce.factory.RedisConfig;
import io.apef.testing.unit.BaseUnitSpec;
import io.apef.repository.RepositoryTestBase;
import io.apef.repository.redis.RedisMockServer;
import io.lettuce.core.AbstractRedisAsyncCommands;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.util.Random;

public abstract class AbstractRedisRepositoryTest extends BaseUnitSpec implements RepositoryTestBase {
    int port = 30000 + new Random().nextInt(30000);

    AbstractRedisAsyncCommands<String, TestObject> asyncCommands;
    LettuceCommandsFactory commandsFactory;

    @BeforeClass
    public void setup() throws Exception {
        super.beforeClass();
        RedisMockServer.startMockServer(port);
        commandsFactory =
                new LettuceCommandsFactory(new RedisConfig()
                        .setCluster(this.isCluster())
                        .setAddresses(redisAddress()));
        asyncCommands =
                commandsFactory.asyncCommands(new TestObjectCodec());
    }


    protected TestContextBuilder testContextBuilder(String name) {
        return (TestContextBuilder) new TestContextBuilder()
                .name("redis" + name);
    }

    @Setter
    @Accessors(fluent = true)
    public class TestContextBuilder extends RepositoryTestBase.TestContextBuilder {
        private String tableName;

        @Override
        public TestContext build() {
            try {
                RedisRepositoryConfig repositoryConfig = new RedisRepositoryConfig();
                repositoryConfig.setName(this.name);
                repositoryConfig.setTableName(this.tableName)
                        .setExpireSecs(expiration)
                        .setMaxCachedSize(this.maxCacheSize)
                        .setEnableCache(this.enableCache)
                        .setCacheOnly(this.cacheOnly)
                        .setKeyMapper(request -> ((TestObject) request).id())
                ;

                RedisLettuceRepository<String, TestObject> lettuceRepository =
                        new RedisLettuceRepository<>(repositoryConfig, asyncCommands);
                TestContext testContext = new TestContext();
                testContext.sourceChannel(new BusinessChannelImpl(new ChannelConfig()
                        .setName("test").setQueueSize(8092)))
                        .repositoryConfig(repositoryConfig);
                testContext.sourceChannel().start();
                testContext.repositoryChannel(lettuceRepository.repositoryChannel());
                testContext.repositoryChannelPipe(lettuceRepository.repositoryChannelPipe(testContext.sourceChannel()));
                testContext.repositoryChannelStore(lettuceRepository.repositoryStore());
                testContext.repositoryCache(lettuceRepository.repositoryCache());
                return testContext;
            } catch (Exception ex) {
                BaseUnitSpec.log.error("", ex);
            }
            return null;
        }
    }

    @AfterClass
    public void teardown() {
        try {
            RedisMockServer.stopMockServer(port);
        } catch (Exception e) {
        }
        try {
            if (asyncCommands != null)
                asyncCommands.shutdown(true);
        } catch (Exception e) {
        }
        try {
            if (commandsFactory != null)
                commandsFactory.close();
        } catch (Exception e) {
        }
        BaseUnitSpec.log.info("Stop tests of " + this.getClass().getSimpleName());
    }

    protected String redisAddress() {
        return "redis://localhost:" + port;
    }

    protected boolean isCluster() {
        return false;
    }
}
