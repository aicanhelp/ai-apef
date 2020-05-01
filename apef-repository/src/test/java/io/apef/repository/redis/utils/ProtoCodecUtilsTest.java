package io.apef.repository.redis.utils;


import io.apef.testing.unit.BaseUnitSpec;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;


public class ProtoCodecUtilsTest extends BaseUnitSpec {

    @Test
    public void testSchema90() {

        Map<? extends Object, ?> map = new HashMap<>();
        doTest(1);
        doTest(new byte[]{});
        doTest(map);
    }

    private void doTest(Object o) {
        try {
            log.info("-----{},{}" ,o.getClass().isInterface(),o.getClass().isMemberClass());
            Schema schema = RuntimeSchema.createFrom(o.getClass());


            log.info("----{}", ProtoCodecUtils.build(o, schema).length);
        } catch (Exception ex) {
            log.error(ex.getMessage() + ":" + o.getClass());
        }
    }
}