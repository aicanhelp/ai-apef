package io.apef.base.config.utils;

import io.apef.testing.unit.BaseUnitSpec;
import io.apef.testing.unit.UnitSpec;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.testng.annotations.Test;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class ConfigurationLoaderTest extends BaseUnitSpec {

    @Test
    public void testLoadOrder() {
        Config config1 = ConfigurationLoader.loadConfiguration(Config.class,
                "utils/test-config-1.xml", "utils/test-config-2.xml");
        log.info(config1.toString());
        Config config2 = loadConfiguration(Config.class,
                "utils/test-config-2.xml", "utils/test-config-1.xml");
        log.info(config2.toString());
    }

    @Test
    public void testCustomEditor() {
        Map<Class, PropertyEditor> map = new HashMap<>();
        map.put(CustomField.class, new CustomPropertyEditor());
        Config config = ConfigurationLoader.loadConfiguration(Config.class, map, "utils/customeditor.xml");
        assertEquals(config.getCustomField().getName(), "customValue");
    }

    @Data
    public static class Config {
        private String name;
        private int value;
        private CustomField customField;

        public String toString() {
            return UnitSpec.toSString(this);
        }
    }

    @Data
    @AllArgsConstructor
    public static class CustomField {
        private String name;
    }

    public static class CustomPropertyEditor extends PropertyEditorSupport {
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            setValue(new CustomField(text));
        }
    }
}