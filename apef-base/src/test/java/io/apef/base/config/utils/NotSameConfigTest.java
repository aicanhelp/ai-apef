package io.apef.base.config.utils;

import io.apef.base.config.validation.ConfigValidator;
import io.apef.base.config.validation.NoSameConfig;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class NotSameConfigTest {

    public static class NotSameConfiguration implements NoSameConfig {
        @NoSame(type = String.class)
        Items items = new Items();

        public static class Items implements NoSameConfig {
            private String a1 = "1";
            private String a2 = "1";
            private String a3,a4;

        }
    }


    @Test
    public void test() {
        NotSameConfiguration testConfig = new NotSameConfiguration();
        try {
            ConfigValidator.validate(testConfig);
            fail("");
        } catch (Exception ex) {

        }
        testConfig.items.a2="2";
        ConfigValidator.validate(testConfig);
    }
}