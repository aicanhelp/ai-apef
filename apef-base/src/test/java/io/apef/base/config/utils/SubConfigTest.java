package io.apef.base.config.utils;


import io.apef.testing.unit.BaseUnitSpec;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.testng.annotations.Test;

public class SubConfigTest extends BaseUnitSpec {
    @Test
    public void testSubConfig() {
        MainConfig mainConfig = ConfigurationLoader.loadConfiguration(MainConfig.class, "utils/subconfig.xml");
        log.info("" + mainConfig);
    }

    @Data
    public static class MainConfig {
        private String name;

        private SubName subName1 = new SubName("A1");
        private SubName subName2 = new SubName("A2");

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public class SubName {
            private String subName;

            public String getSubName() {
                return name + "/" + this.subName;
            }
        }
    }
}
