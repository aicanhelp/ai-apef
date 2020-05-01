package io.apef.base.config.factory;


import io.apef.testing.unit.BaseUnitSpec;
import io.apef.testing.unit.UnitSpec;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class XFactoryBaseTest extends BaseUnitSpec {

    @Test
    public void testCorrectConfig() {
        TestConfigs testConfigs = loadConfiguration(TestConfigs.class,
                "factory/factory-config-correct.xml");
        for(int i=0;i<11;i++){
            assertNotNull(testConfigs.configs.config("config"+i));
        }
    }

    @Test
    public void testSameNameConfig() {
        TestConfigs testConfigs = loadConfiguration(TestConfigs.class,
                "factory/factory-config-samename.xml");
        assertNull(testConfigs);
    }

    @Test
    public void testSameNameExtConfig() {
        TestConfigs testConfigs = loadConfiguration(TestConfigs.class,
                "factory/factory-config-samename-ext.xml");
        assertNull(testConfigs);
    }

    @Test
    public void testInstanceFactory() {
        TestConfigs testConfigs = loadConfiguration(TestConfigs.class,
                "factory/factory-config-correct.xml");
        InstanceFactory instanceFactory = new InstanceFactory(testConfigs.configs);
        for(int i=0;i<11;i++){
            assertNotNull(instanceFactory.instance("config"+i));
            assertTrue(instanceFactory.instance("config"+i)==instanceFactory.instance("config"+i));
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @Accessors(chain = true)
    public static class Config extends XConfigBaseX<Config> {
        private String value;

        @Override
        public String toString() {
            return UnitSpec.toSString(this);
        }

        @Override
        public Config copy() {
            return new Config().setValue(this.value);
        }
    }

    public static class InstanceFactory extends XFactoryBase<Instance, Config> {

        public InstanceFactory(XFactoryConfig<Config> factoryConfig) {
            super(factoryConfig);
        }

        @Override
        protected Instance newInstance(Config config) throws Exception {
            return new Instance(config);
        }

        @Override
        protected void close(Instance instance) {

        }
    }

    @Data
    @Accessors(chain = true)
    public static class TestConfigs {
        XFactoryConfigX<Config> configs = new XFactoryConfigX<>();

        @Override
        public String toString() {
            return UnitSpec.toSString(this);
        }
    }

    public static class Instance {
        public Instance(Config config) {

        }
    }
}