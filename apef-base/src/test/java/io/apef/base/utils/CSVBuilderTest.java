package io.apef.base.utils;

import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class CSVBuilderTest extends BaseUnitSpec {
    @Test
    public void testAppend() {
        String s = "1,2,a,4,5";
        CSVBuilder csvBuilder = new CSVBuilder();
        csvBuilder.append("1").append("2").append("a").append("4").append("5");
        assertEquals(s, csvBuilder.toString());
    }
}