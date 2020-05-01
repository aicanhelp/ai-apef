package io.apef.base.config.utils;

import io.apef.testing.unit.BaseUnitSpec;
import io.apef.testing.unit.UnitSpec;
import lombok.Data;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;


public class ConfigUtilsTest extends BaseUnitSpec {

    @Test
    public void testConvertFromMap() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", "main");
        Map<String, Object> subs = new HashMap<>();
        Map<String, Object> subConfig = new HashMap<>();
        subConfig.put("name", "a");
        subConfig.put("count", 1);
        subs.put("sub1", subConfig);
        properties.put("subs", subs);
        Config config = ConfigUtils.convertFromMap(Config.class, properties);
        log.info("" + config);
    }

    @Test
    public void testUpdate() {
        Config config = new Config();

        ConfigUtils.update(String.class, config, (name, old) -> "value");

        assertEquals(config.getName(), "value");
    }

    @Data
    public static class Config {
        private String name;
        private Map<String, SubConfig> subs = new HashMap<>();

        @Override
        public String toString() {
            return UnitSpec.toSString(this);
        }
    }

    @Data
    public static class SubConfig {
        private String name;
        private int count;

        @Override
        public String toString() {
            return UnitSpec.toSString(this);
        }
    }
}