package io.apef.base.config.utils;

import io.apef.base.config.validation.ConfigValidator;
import io.apef.base.config.validation.OneEnabledConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.Test;

import static org.junit.Assert.fail;


public class EnabledConfigTest {

    @Test
    public void test() {
        TestConfig testConfig = new TestConfig();
        try {
            ConfigValidator.validate(testConfig);
            fail();
        } catch (Exception ex) {

        }
        testConfig.config.a.setEnabled(true);
        ConfigValidator.validate(testConfig);
        testConfig.config.b.setEnabled(true);
        try {
            ConfigValidator.validate(testConfig);
            fail();
        } catch (Exception ex) {

        }
    }

    private static class TestConfig {
        @OneEnabledConfig.OneEnabled
        TestEnabledConfig config = new TestEnabledConfig();
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    private static class TestEnabledConfig extends OneEnabledConfig {
        private EnabledItem1 a = new EnabledItem1();
        private EnabledItem2 b = new EnabledItem2();
    }

    private static class EnabledItem1 extends OneEnabledConfig.EnabledConfigBase {

    }

    private static class EnabledItem2 extends OneEnabledConfig.EnabledConfigBase {

    }
}