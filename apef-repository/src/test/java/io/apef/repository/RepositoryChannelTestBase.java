package io.apef.repository;

import com.codahale.metrics.Timer;
import io.apef.core.channel.BusinessChannelImpl;
import io.apef.core.channel.ChannelConfig;
import io.apef.metrics.ApefMetric;
import io.apef.metrics.ApefMetricsFactory;
import io.apef.testing.unit.BaseUnitSpec;
import io.apef.repository.channel.*;

import io.apef.metrics.Metricable;
import io.apef.metrics.item.MetricItemTimer;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.map.HashedMap;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.spy;

public abstract class RepositoryChannelTestBase
        extends BaseUnitSpec implements RepositoryTestBase {

    protected TestContextBuilder testContextBuilder(String name) {
        return (TestContextBuilder) new TestContextBuilder().name(name);
    }

    class TestContextBuilder extends RepositoryTestBase.TestContextBuilder {
        @Override
        public TestContext build() {
            TestContext testContext = new TestContext();
            testContext.sourceChannel(new BusinessChannelImpl(new ChannelConfig()
                    .setName("test_" + this.name).setQueueSize(8092)))
                    .repositoryConfig(new RepositoryConfig()
                            .setEnableCache(enableCache)
                            .setCacheOnly(this.cacheOnly)
                            .setMaxCachedSize(this.maxCacheSize)
                            .setEnableCacheMetrics(this.metrics)
                            .setKeyMapper(request -> ((TestObject) request).id()));
            testContext.sourceChannel.start();
            testContext.repositoryConfig.setName(name + new Random().nextInt());
            testContext.repositoryCache(new LRURepositoryCache<>(testContext.repositoryConfig));
            testContext.repositoryChannelStore = new RepositoryStoreForTest(name + "store").metrics(this.metrics);
            testContext.repositoryChannel = new RepositoryChannel<>(testContext.repositoryConfig,
                    testContext.repositoryCache, testContext.repositoryChannelStore);
            testContext.repositoryChannel.start();
            testContext.repositoryChannelPipe = new RepositoryChannelPipeImpl<>(testContext.sourceChannel,
                    testContext.repositoryChannel,
                    new RepositoryChannelPipeInterceptor(testContext.repositoryCache, cacheOnly));
            return testContext;
        }
    }

    static class RepositoryStoreForTest implements RepositoryChannelStore<String, TestObject>, Metricable {
        private Map<String, TestObject> store = new ConcurrentHashMap<>();
        @Getter
        @Accessors(fluent = true)
        private ApefMetric metric;
        private MetricItemTimer timer, saveTimer, getAllTimer, putAllTimer;

        public RepositoryStoreForTest(String name) {
            metric = ApefMetricsFactory
                    .defaultMetrics()
                    .vexMetric(name + "_TestRepository");
            timer = metric.timer("get");
            putAllTimer = metric.timer("putAll");
            getAllTimer = metric.timer("getAll");
            saveTimer = metric.timer("save");
            this.metrics(true);
        }

        protected RepositoryStoreForTest metrics(boolean enable) {
            saveTimer.enabled(enable);
            putAllTimer.enabled(enable);
            getAllTimer.enabled(enable);
            timer.enabled(enable);
            return this;
        }

        @Override
        public void get(String key, RepositoryStoreHandler<TestObject> handler) {
            Timer.Context context = timer.start();
            handler.handle(store.get(key), null);
            if (context != null) context.stop();
        }

        @Override
        public void save(TestObject value, RepositoryStoreHandler<Boolean> handler) {
            Timer.Context context = saveTimer.start();
            store.put(value.id(), value);
            handler.handle(true, null);
            if (context != null) context.stop();
        }

        @Override
        public void exists(String key, RepositoryStoreHandler<Boolean> handler) {
            handler.handle(store.containsKey(key), null);
        }

        @Override
        public void getAll(Set<String> keys, RepositoryStoreHandler<Map<String, TestObject>> handler) {
            Timer.Context context = getAllTimer.start();
            Map<String, TestObject> values = new HashedMap();
            keys.forEach(o -> {
                values.put(o, store.get(o));
            });
            handler.handle(values, null);
            if (context != null)
                context.stop();
        }

        @Override
        public void putAll(Map<String, TestObject> values, RepositoryStoreHandler<Boolean> handler) {
            Timer.Context context = putAllTimer.start();
            store.putAll(values);
            handler.handle(true, null);
            if (context != null) context.stop();
        }
    }
}
