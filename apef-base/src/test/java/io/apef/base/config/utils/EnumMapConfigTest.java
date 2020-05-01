package io.apef.base.config.utils;


import io.apef.testing.unit.BaseUnitSpec;
import io.apef.testing.unit.UnitSpec;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class EnumMapConfigTest extends BaseUnitSpec {

    @Test
    public void runTest() {
        EnumMapConfig config = ConfigurationLoader.loadConfiguration(EnumMapConfig.class, "utils/enum-map.yaml");
        log.info(config.toString());
        LiveEnumConfig liveEnumConfig = (LiveEnumConfig) config.enumConfigs.get(TypeEnum.live);
        log.info(liveEnumConfig.toString());
        LiveConfig liveConfig = liveEnumConfig.get("liveConfig1");
        log.info(liveConfig.toString());
    }

    @Data
    public static class EnumMapConfig{
        private Map<TypeEnum, HashMap<String, ? extends IConfig>> enumConfigs = new HashMap<>();

        public EnumMapConfig() {
            this.enumConfigs.put(TypeEnum.live, new LiveEnumConfig());
            this.enumConfigs.put(TypeEnum.vod, new VodEnumConfig());
        }

        @Override
        public String toString() {
            return UnitSpec.toSString(this);
        }
    }

    @Data
    public static class EnumMapConfig2<T> {
        private Map<TypeEnum, T> enumConfigs = new HashMap<>();

        public EnumMapConfig2() {

        }

        @Override
        public String toString() {
            return UnitSpec.toSString(this);
        }
    }

    public interface IEnumConfig {
    }


    @EqualsAndHashCode(callSuper = false)
    @Data
    public static class LiveEnumConfig extends HashMap<String, LiveConfig> implements IEnumConfig {
        private TypeEnum type;

        @Override
        public LiveConfig get(Object key) {
            LiveConfig config = super.get(key);
            if (config == null) config = new LiveConfig();
            this.put((String) key, config);
            return config;
        }

        @Override
        public String toString() {
            return UnitSpec.toSString(this);
        }
    }

    @EqualsAndHashCode(callSuper = false)
    @Data
    public static class VodEnumConfig extends HashMap<String, VodConfig> implements IEnumConfig {
        private TypeEnum type;

        @Override
        public VodConfig get(Object key) {
            return super.getOrDefault(key, new VodConfig());
        }

        @Override
        public String toString() {
            return UnitSpec.toSString(this);
        }

    }

    public interface IConfig {

    }

    @Data
    public static class LiveConfig implements IConfig {
        private String name;

        @Override
        public String toString() {
            return UnitSpec.toSString(this);
        }
    }

    @Data
    public static class VodConfig implements IConfig {
        private int count;

        @Override
        public String toString() {
            return UnitSpec.toSString(this);
        }
    }

    public enum TypeEnum {
        live, vod;

        private Class<?> configClass;
        private Class<?> serviceMaker;
    }
}
