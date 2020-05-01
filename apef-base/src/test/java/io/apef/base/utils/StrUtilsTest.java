package io.apef.base.utils;

import io.apef.testing.unit.BaseUnitSpec;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

@Test
public class StrUtilsTest extends BaseUnitSpec {
    public void testIndex() {
        String source = "12345dabcd4abcd";
        String target = "abcd";

        int index1 = StringUtils.indexOf(source, target);
        int index2 = StrUtils.indexOf(source, target, 2, 5);
        assertTrue(index2 == -1);

        int index3 = StrUtils.indexOf(source, target, 3, 30);
        assertEquals(index3, index1);

        index3 = StrUtils.indexOf(source, target, 0, 30);
        assertEquals(index3, index1);

        index3 = StrUtils.indexOf(source, target, 0, source.length());
        assertEquals(index3, index1);
    }
}