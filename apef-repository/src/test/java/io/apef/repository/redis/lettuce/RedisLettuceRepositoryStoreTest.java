package io.apef.repository.redis.lettuce;

import io.apef.base.utils.Expiration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Test
public class RedisLettuceRepositoryStoreTest extends AbstractRedisRepositoryTest {
    private RedisLettuceRepositoryStore<String, TestObject> repositoryStore;

    private RedisLettuceRepositoryStore<String, TestObject> repositoryStoreWithTable;

    @BeforeClass
    public void setup() throws Exception {
        super.setup();
        this.repositoryStoreWithTable = RedisLettuceRepositoryStore
                .builder(commandsFactory.asyncCommands(new TestObjectCodec()))
                .name("TestRepositoryWithTable")
                .tableName("table")
                .keyMapper(TestObject::id)
                .keyValueMerger((key, value) -> value.id())
                .build();
        this.repositoryStore =
                RedisLettuceRepositoryStore
                        .builder(commandsFactory.asyncCommands(new TestObjectCodec()))
                        .name("TestRepository")
                        .expiration(new Expiration(3, 0))
                        .keyMapper(TestObject::id)
                        .keyValueMerger((key, value) -> value.id())
                        .build();
    }

    public void testSaveAndGetAndExists() throws Exception {
        this.doTestSaveAndGetAndExists(this.repositoryStore);
        this.doTestSaveAndGetAndExists(this.repositoryStoreWithTable);
    }


    public void testPutAllAndGetAll() throws Exception {
        this.doPutAllAndGetAll(this.repositoryStore);
        this.doPutAllAndGetAll(this.repositoryStoreWithTable);
    }

    @Test(enabled = false)
    public void testMetrics() {
        repositoryStore.metric().enable(true);
        for (int i = 0; i < 10; i++) {
            Blocker blocker1 = new Blocker();
            repositoryStore.save(new TestObject("key_" + i, "value"), (returnValue, ex) -> {
                blocker1.assertTrue(returnValue).end();
            });
            blocker1.awaitEnd();
        }

        new Blocker().awaitEnd(3000);
    }


    private void doPutAllAndGetAll(RedisLettuceRepositoryStore<String, TestObject> repositoryStore) throws Exception {
        Set<String> keys = new HashSet<>();
        Map<String, TestObject> values = new HashMap<>();
        for (int i = 0; i < 20; i++) {
            String key = "key_" + i;
            keys.add(key);
            values.put(key, new TestObject(key, "value_" + i));
        }

        Blocker blocker = new Blocker();
        repositoryStore.putAll(values, (returnValue, ex) -> {
            blocker.assertTrue(returnValue).end();
        });
        blocker.awaitEnd();

        Blocker blocker1 = new Blocker();
        repositoryStore.getAll(keys, (returnValue, ex) -> {
            for (Map.Entry<String, TestObject> object : returnValue.entrySet()) {
                blocker1.assertEquals(object.getKey(), object.getValue().id());
            }
            blocker1.end();
        });
        blocker1.awaitEnd();
    }

    private void doTestSaveAndGetAndExists(RedisLettuceRepositoryStore<String, TestObject> repositoryStore) throws Exception {
        Blocker blocker1 = new Blocker();
        repositoryStore.save(new TestObject("key_1", "value"), (returnValue, ex) -> {
            blocker1.assertTrue(returnValue).end();
        });
        blocker1.awaitEnd();

        Blocker blocker2 = new Blocker();

        repositoryStore.get("key_1", (returnValue, ex) -> {
            blocker2.assertEquals(returnValue.value(), "value").end();
        });
        blocker2.awaitEnd();

        Blocker blocker3 = new Blocker();

        repositoryStore.exists("key_1", (returnValue, ex) -> {
            blocker3.assertTrue(returnValue).end();
        });
        blocker3.awaitEnd();
    }

}