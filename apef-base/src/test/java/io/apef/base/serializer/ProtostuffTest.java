package io.apef.base.serializer;

import io.apef.testing.unit.BaseUnitSpec;
import io.protostuff.runtime.RuntimeSchema;
import org.testng.annotations.Test;


public class ProtostuffTest extends BaseUnitSpec {

    private static class AAA {

    }

    private static class BBBB extends AAA {

    }

    @Test
    public void test() {
        testType(new AAA());
        testType(new BBBB());
    }

    private void testType(Object value) {
        log.info("" + RuntimeSchema.getSchema(value.getClass()));
    }
}
