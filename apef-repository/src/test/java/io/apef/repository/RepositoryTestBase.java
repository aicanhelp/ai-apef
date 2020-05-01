package io.apef.repository;

import io.apef.core.channel.BusinessChannel;
import io.apef.testing.unit.BaseUnitSpec;
import io.apef.base.serializer.DataSerializer;
import io.apef.base.utils.Bytes;
import io.apef.repository.channel.*;
import io.apef.metrics.Metricable;
import io.lettuce.core.codec.RedisCodec;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;


import static io.apef.testing.benchmark.Benchmark.benchmark;
import static org.testng.Assert.assertEquals;


public interface RepositoryTestBase {
    Logger log = LoggerFactory.getLogger(RepositoryTestBase.class);

    @Setter
    @Accessors(fluent = true)
    abstract class TestContextBuilder {
        protected String name;
        protected boolean metrics;
        protected int expiration = -1;
        protected boolean enableCache = true;
        protected int maxCacheSize = 10000;
        protected boolean cacheOnly = false;

        public abstract TestContext build();
    }

    @Data
    @Accessors(fluent = true)
    class TestContext {
        protected RepositoryConfig repositoryConfig;
        protected BusinessChannel sourceChannel;
        protected RepositoryChannel<String, TestObject> repositoryChannel;
        protected RepositoryCache<String, TestObject> repositoryCache;
        protected RepositoryChannelPipe<String, TestObject> repositoryChannelPipe;
        protected RepositoryChannelStore<String, TestObject> repositoryChannelStore;

        public void close() {
            sourceChannel.close();
            repositoryChannel.close();
        }

    }

    default void benchmarkSave(RepositoryChannelPipe<String, TestObject> repositoryChannelPipe, int iterations) {
        benchmark().threads(1).concurrency(500).iterations(iterations)
                .rounds(10)
                .reportInterval(5)
                .benchmarkTask((index, runnerContext) -> {
                    repositoryChannelPipe.srcChannel().execute(() -> {
                        repositoryChannelPipe.save()
                                .value(new TestObject("key_" + index, null))
                                .onFailure((errMsg, cause) -> {
                                    log.error(errMsg, cause);
                                    runnerContext.done(index);
                                })
                                .onSuccess((responseContent) -> {
                                    runnerContext.done(index);
                                })
                                .end();
                    });
                }).start();
    }

    default void benchmarkGet(RepositoryChannelPipe<String, TestObject> repositoryChannelPipe, int iterations) {
        benchmarkGet(repositoryChannelPipe, iterations, -1);
    }

    default void benchmarkGet(RepositoryChannelPipe<String, TestObject> repositoryChannelPipe, int iterations, int timeout) {
        benchmark().threads(1).concurrency(500)
                .iterations(iterations)
                .rounds(10)
                .reportInterval(5)
                .benchmarkTask((index, runnerContext) -> {
                    repositoryChannelPipe.srcChannel().execute(() -> {
                        repositoryChannelPipe.get()
                                .timeout(timeout)
                                .key("key_" + index)
                                .onFailure((errMsg, cause) -> {
                                    log.error(errMsg, cause);
                                    runnerContext.done(index);
                                })
                                .onSuccess((responseContent) -> {
                                    runnerContext.done(index);
                                })
                                .end();
                    });
                }).start();
    }

    default void benchmarkSaveAndGet(RepositoryChannelPipe<String, TestObject> repositoryChannelPipe, int iterations) {
        benchmarkSave(repositoryChannelPipe, iterations);
        benchmarkGet(repositoryChannelPipe, iterations);
    }

    default void doTestSaveExistsGet(TestContext testContext) {

        long count = 0;
        if (testContext.repositoryChannelStore != null &&
                testContext.repositoryChannelStore instanceof Metricable) {
            count = ((Metricable) testContext.repositoryChannelStore).metric().timer("get").count();
        }
        BaseUnitSpec.Blocker blocker1 = new BaseUnitSpec.Blocker();
        testContext.repositoryChannelPipe.srcChannel().execute(() -> {
            testContext.repositoryChannelPipe.save().value(new TestObject("1", "a"))
                    .onSuccess((response) -> {
                        blocker1.assertTrue(response).end();
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker1.failAndEnd("doTestSaveExistsGet Failed:" + errMsg, cause);
                    })
                    .end();
        });
        blocker1.awaitEnd();

        BaseUnitSpec.Blocker blocker2 = new BaseUnitSpec.Blocker();
        testContext.repositoryChannelPipe.srcChannel().execute(() -> {
            testContext.repositoryChannelPipe.exists().key("1")
                    .onSuccess((response) -> {
                        blocker2.assertTrue(response);
                        blocker2.end();
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker1.failAndEnd(errMsg);
                    })
                    .end();
        });

        blocker2.awaitEnd();

        BaseUnitSpec.Blocker blocker3 = new BaseUnitSpec.Blocker();
        testContext.repositoryChannelPipe.srcChannel().execute(() -> {
            testContext.repositoryChannelPipe.get().key("1")
                    .onSuccess((response) -> {
                        blocker3.assertEquals(response.id(), "1");
                        blocker3.end();
                    }).onFailure((errMsg, cause) -> {
                blocker1.failAndEnd(errMsg);
            }).end();
        });

        blocker3.awaitEnd();

        if (testContext.repositoryChannelStore != null &&
                testContext.repositoryChannelStore instanceof Metricable) {
            //all hit cache
            assertEquals(((Metricable) testContext.repositoryChannelStore).metric().timer("get").count(), count);
        }

        testContext.close();
    }

    default void doTestCacheOnly(TestContext testContext) {
        long getCount = 0;
        long saveCount = 0;
        if (testContext.repositoryChannelStore != null && testContext.repositoryChannelStore instanceof Metricable) {
            getCount = ((Metricable) testContext.repositoryChannelStore).metric().timer("get").count();
            saveCount = ((Metricable) testContext.repositoryChannelStore).metric().timer("save").count();
        }

        BaseUnitSpec.Blocker blocker1 = new BaseUnitSpec.Blocker();
        testContext.repositoryChannelPipe.srcChannel().execute(() -> {
            testContext.repositoryChannelPipe.save().value(new TestObject("1", "a"))
                    .onSuccess((response) -> {
                        blocker1.assertTrue(response);
                        blocker1.end();
                    })
                    .onFailure((errMsg, cause) -> {
                    })
                    .end();
        });
        blocker1.awaitEnd();

        BaseUnitSpec.Blocker blocker2 = new BaseUnitSpec.Blocker();
        testContext.repositoryChannelPipe.srcChannel().execute(() -> {
            testContext.repositoryChannelPipe.exists().key("1")
                    .onSuccess((response) -> {
                        blocker2.assertTrue(response);
                        blocker2.end();
                    })
                    .onFailure((errMsg, cause) -> {
                    })
                    .end();
        });

        blocker2.awaitEnd();

        BaseUnitSpec.Blocker blocker3 = new BaseUnitSpec.Blocker();
        testContext.repositoryChannelPipe.srcChannel().execute(() -> {
            testContext.repositoryChannelPipe.get().key("1")
                    .onSuccess((response) -> {
                        blocker3.assertEquals(response.id(), "1");
                        blocker3.end();
                    }).onFailure((errMsg, cause) -> {
            }).end();
        });

        blocker3.awaitEnd();

        if (testContext.repositoryChannelStore != null &&
                testContext.repositoryChannelStore instanceof Metricable) {
            //all hit cache
            assertEquals(((Metricable) testContext.repositoryChannelStore).metric().timer("get").count(), getCount);
            assertEquals(((Metricable) testContext.repositoryChannelStore).metric().timer("save").count(), saveCount);
        }
    }

    default void doTestPutAllGetAll(TestContext testContext) {
        long count = 0;
        if (testContext.repositoryChannelStore != null &&
                testContext.repositoryChannelStore instanceof Metricable) {
            count = ((Metricable) testContext.repositoryChannelStore).metric().timer("getAll").count();
        }
        Map<String, TestObject> putValues = new HashedMap();
        for (int i = 0; i < 10; i++) {
            putValues.put("" + i, new TestObject("" + i, ""));
        }

        BaseUnitSpec.Blocker blocker1 = new BaseUnitSpec.Blocker();
        testContext.repositoryChannelPipe.srcChannel().execute(() -> {
            testContext.repositoryChannelPipe.putAll().values(putValues)
                    .onSuccess((response) -> {
                        blocker1.assertTrue(response);
                        blocker1.end();
                    })
                    .onFailure((errMsg, cause) -> {
                        log.error(errMsg, cause);
                    })
                    .end();
        });

        blocker1.awaitEnd();

        BaseUnitSpec.Blocker blocker2 = new BaseUnitSpec.Blocker();
        testContext.repositoryChannelPipe.srcChannel().execute(() -> {
            testContext.repositoryChannelPipe.getAll().keys(putValues.keySet())
                    .onSuccess((response) -> {
                        blocker2.assertEquals(response.size(), putValues.size());
                        blocker2.end();
                    })
                    .onFailure((errMsg, cause) -> {
                        log.error(errMsg, cause);
                    })
                    .end();
        });

        blocker2.awaitEnd();

        if (testContext.repositoryChannelStore != null &&
                testContext.repositoryChannelStore instanceof Metricable) {
            //all hit cache
            assertEquals(((Metricable) testContext.repositoryChannelStore).metric().timer("getAll").count(), count);
        }
        testContext.close();
    }

    default void doTestReadThrough(TestContext testContext) {
        Set<String> keySet = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            keySet.add("rkey_" + i);
            testContext.repositoryChannelStore.save(new TestObject("rkey_" + i, null),
                    (returnValue, ex) -> {
                    });
        }

        BaseUnitSpec.Blocker blocker1 = new BaseUnitSpec.Blocker();
        testContext.repositoryChannelPipe.get()
                .key("rkey_1")
                .readThrough(false)
                .onSuccess((responseContent) -> {
                    blocker1.assertNull(responseContent).end();
                })
                .onFailure((errMsg, cause) -> {
                    blocker1.failAndEnd("Test1 failed: " + errMsg, cause);
                })
                .end();
        blocker1.awaitEnd();

        BaseUnitSpec.Blocker blocker2 = new BaseUnitSpec.Blocker();
        testContext.repositoryChannelPipe.get()
                .key("rkey_1")
                .onSuccess((responseContent) -> {
                    blocker2.assertNotNull(responseContent).end();
                })
                .onFailure((errMsg, cause) -> {
                    blocker1.failAndEnd("Test2 failed: " + errMsg, cause);
                })
                .end();
        blocker2.awaitEnd();

        BaseUnitSpec.Blocker blocker3 = new BaseUnitSpec.Blocker();
        testContext.repositoryChannelPipe.exists()
                .key("rkey_2")
                .readThrough(false)
                .onSuccess((responseContent) -> {
                    blocker3.assertFalse(responseContent).end();
                })
                .onFailure((errMsg, cause) -> {
                    blocker1.failAndEnd("Test3 failed: " + errMsg, cause);
                })
                .end();
        blocker3.awaitEnd();

        BaseUnitSpec.Blocker blocker4 = new BaseUnitSpec.Blocker();
        testContext.repositoryChannelPipe.exists()
                .key("rkey_2")
                .onSuccess((responseContent) -> {
                    blocker4.assertTrue(responseContent).end();
                })
                .onFailure((errMsg, cause) -> {
                    blocker1.failAndEnd("Test4 failed: " + errMsg, cause);
                })
                .end();
        blocker4.awaitEnd();

        BaseUnitSpec.Blocker blocker5 = new BaseUnitSpec.Blocker();
        testContext.repositoryChannelPipe.getAll()
                .keys(keySet)
                .readThrough(false)
                .onSuccess((responseContent) -> {
                    blocker5.assertEquals(responseContent.values().stream().filter(Objects::nonNull).count(), (long) 2).end();
                })
                .onFailure((errMsg, cause) -> {
                    blocker1.failAndEnd("Test5 failed: " + errMsg, cause);
                })
                .end();
        blocker5.awaitEnd();

        BaseUnitSpec.Blocker blocker6 = new BaseUnitSpec.Blocker();
        testContext.repositoryChannelPipe.getAll()
                .keys(keySet)
                .onSuccess((responseContent) -> {
                    blocker6.assertEquals(responseContent.values().stream().filter(Objects::nonNull).count(), (long) 5).end();
                })
                .onFailure((errMsg, cause) -> {
                    blocker1.failAndEnd("Test6 failed: " + errMsg, cause);
                })
                .end();
        blocker6.awaitEnd();
        testContext.close();
    }

    default void doTestCacheStats(TestContext testContext) {
        for (int i = 0; i < 5; i++) {
            testContext.repositoryChannelStore.save(new TestObject("key_" + i, null),
                    (returnValue, ex) -> {
                    });
        }
        for (int i = 0; i < 10; i++) {
            String key = "key_" + i;
            //because the bussiness channel is running in another thread,
            //here need a blocker to wait the end of this process
            BaseUnitSpec.Blocker blocker1 = new BaseUnitSpec.Blocker();
            testContext.repositoryChannelPipe.get().key(key)
                    .onSuccess((response) -> {
                        blocker1.end();
                    })
                    .onFailure((errMsg, cause) -> {
                    })
                    .end();
            blocker1.awaitEnd();

            BaseUnitSpec.Blocker blocker2 = new BaseUnitSpec.Blocker();
            testContext.repositoryChannelPipe.get().key(key)
                    .onSuccess((response) -> {
                        blocker2.end();
                    })
                    .onFailure((errMsg, cause) -> {
                    })
                    .end();
            blocker2.awaitEnd();
        }

        assertEquals(testContext.repositoryCache.stats().hitCount(), 5);
        assertEquals(testContext.repositoryCache.stats().missCount(), 15);
        testContext.close();
    }

    @Data
    @Accessors(fluent = true)
    @AllArgsConstructor
    class TestObject {
        private String id;
        private Object value;
    }

    @Slf4j
    class TestObjectCodec implements RedisCodec<String, TestObject> {

        DataSerializer<TestObject> dataSerializer = DataSerializer.protoStuff(TestObject.class);

        @Override
        public String decodeKey(ByteBuffer bytes) {
            return Bytes.wrap(bytes).toString();
        }

        @Override
        public TestObject decodeValue(ByteBuffer bytes) {
            try {
                return dataSerializer.deserialize(Bytes.wrap(bytes).getBytes());
            } catch (Exception ex) {
                log.error("", ex);
            }
            return null;
        }

        @Override
        public ByteBuffer encodeKey(String key) {
            return ByteBuffer.wrap(key.getBytes());
        }

        @Override
        public ByteBuffer encodeValue(TestObject value) {
            try {
                return ByteBuffer.wrap(dataSerializer.serialize(value));
            } catch (Exception ex) {
                log.error("", ex);
            }
            return null;
        }
    }
}
