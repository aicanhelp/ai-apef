package io.apef.base.utils;

import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;

import static io.apef.base.utils.Bytes.byteBufOf;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;


public class CSVReaderTest extends BaseUnitSpec {
    @Test
    public void testCommonRead() {
        String s = "1,2,a,4,,5,";
        CSVReader csvReader = new CSVReader(byteBufOf(s));
        assertEquals(1, csvReader.readInt());
        assertEquals(2, csvReader.readInt());
        assertEquals("a", csvReader.readString());
        assertEquals(4, csvReader.readInt());
        assertEquals("", csvReader.readString());
        assertEquals(5, csvReader.readInt());
        assertEquals("", csvReader.readString());
    }

    @Test
    public void testEmptyRead() {
        String s2 = ",,,";
        CSVReader csvReader2 = new CSVReader(byteBufOf(s2));
        assertEquals("", csvReader2.readString());
        assertEquals("", csvReader2.readString());
        assertEquals("", csvReader2.readString());
        assertEquals("", csvReader2.readString());
    }

    @Test
    public void testWholeRead() {
        String s2 = "1234567";
        CSVReader csvReader2 = new CSVReader(byteBufOf(s2));
        assertEquals(1234567, csvReader2.readInt());
    }
}