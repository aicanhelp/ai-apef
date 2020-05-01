package io.apef.base.config.utils;

import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.Properties;

import static org.testng.Assert.*;

public class Xml2PropertiesTest extends BaseUnitSpec {
    private final static String configFile = "utils/xml-properties.xml";
    private final static String configFile2 = "utils/xml-properties-2.xml";
    private final static String configFile3 = "utils/xml-properties-3.xml";

    @Test
    public void testLoadFromClasspath() {
        try {
            Properties properties = Xml2Properties.load(configFile);
            assertNotNull(properties.getProperty("test.name"));
            assertNotNull(properties.getProperty("test.case.id.value"));
            properties = Xml2Properties.load(configFile2);
            assertNotNull(properties.getProperty("root.test.name"));
            assertNotNull(properties.getProperty("root.test.case.id.value"));
            properties = Xml2Properties.load(configFile3);
            assertNotNull(properties.get("test.cases.case[0].ids.id[0].value"));
        } catch (Exception ex) {
            fail("", ex);
        }
    }

    @Test
    public void testLoadFromInputStream() {
        try {
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(configFile);
            Properties properties = Xml2Properties.load(inputStream);
            assertNotNull(properties.getProperty("test.name"));
        } catch (Exception ex) {
            fail();
        }
    }
}