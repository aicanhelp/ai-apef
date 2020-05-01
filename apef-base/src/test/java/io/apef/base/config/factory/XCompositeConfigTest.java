package io.apef.base.config.factory;

import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

public class XCompositeConfigTest {

    @Test
    public void testConfigs() throws Exception {
        TestConfig config = new TestConfig();
        List<String> fields = config.configs(String.class, true);
        assertTrue(fields.size() == 3);

        fields = config.configs(String.class);
        assertTrue(fields.size() == 0);
    }

    private static class TestConfig extends XCompositeConfig {
        private String a, b, c;
    }
}