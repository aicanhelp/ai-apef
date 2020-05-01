package io.apef.repository.lrucache;

import io.apef.core.channel.BusinessChannelImpl;
import io.apef.core.channel.ChannelConfig;
import io.apef.repository.RepositoryTestBase;
import io.apef.repository.redis.lettuce.RedisRepositoryConfig;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.testng.annotations.Test;

@Test
public class LRUCacheRepositoryTest implements RepositoryTestBase {

    protected TestContextBuilder testContextBuilder(String name) {
        return (TestContextBuilder) new TestContextBuilder()
                .name("redis" + name);
    }

    @Setter
    @Accessors(fluent = true)
    public class TestContextBuilder extends RepositoryTestBase.TestContextBuilder {

        @Override
        public TestContext build() {
            RedisRepositoryConfig repositoryConfig = new RedisRepositoryConfig();
            repositoryConfig
                    .setKeyMapper(request -> ((TestObject) request).id()
                    )
            ;

            LRUCacheRepository<String, TestObject> lettuceRepository =
                    new LRUCacheRepository<>(repositoryConfig);
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
        }
    }

    public void testSaveExistsGet() {
        doTestSaveExistsGet(testContextBuilder("testSaveExistsGet").build());
    }

    public void testPutAllGetAll() {
        doTestPutAllGetAll(testContextBuilder("testPutAllGetAll").build());
    }
}