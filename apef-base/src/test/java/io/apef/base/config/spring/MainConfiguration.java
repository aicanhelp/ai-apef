package io.apef.base.config.spring;

import io.apef.testing.unit.UnitSpec;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(
        prefix = "test-app",
        ignoreUnknownFields = false
//        ,locations = {"classpath:spring/${spring.config.name}-ex-${spring.profiles.active}.yaml",
//                "classpath:spring/${spring.config.name}-ex-${spring.profiles.active}.yml",
//                "classpath:spring/${spring.config.name}-ex-${spring.profiles.active}.properties",
//                "classpath:spring/${spring.config.name}-ex-${spring.profiles.active}.xml"
//        }
)
@Data
@Accessors(chain = true)
public class MainConfiguration {
    protected final static Logger log = LoggerFactory.getLogger(MainConfiguration.class);
    private String name1;
    private String name2;
    private String name3;
    private String name4;
    private SubMap sub;
    private List<SubItem> subItems;
    private List<Type> types;
    private Map<Type, SubItem> itemTypeMap;

    private List<Type> getTypes() {
        return this.types;
    }

    @Data
    public static class SubConfiguration {
        private String name;
        private int count;
        private String address;

        @Override
        public String toString() {
            return UnitSpec.toSString(this);
        }
    }

    @EqualsAndHashCode(callSuper = false)
    @Data
    public static class SubMap extends HashMap<String, SubConfiguration> {
        //private String name;
        public SubMap() {
            log.info("dddd");
        }

        public void put(String key, String name) {

        }

        @Override
        public SubConfiguration put(String key, SubConfiguration value) {
            value.setAddress("---put----" + value.getName());
            return super.put(key, value);
        }

        @Override
        public SubConfiguration get(Object key) {
           return super.get(key);
        }

        @Override
        public SubConfiguration getOrDefault(Object key, SubConfiguration defaultValue) {
            log.info("------getOrDefault--" + key);
            return super.getOrDefault(key, defaultValue);
        }

        @Override
        public SubConfiguration putIfAbsent(String key, SubConfiguration value) {
            value.setName("---" + value.getName());
            return super.putIfAbsent(key, value);
        }

        @Override
        public void putAll(Map<? extends String, ? extends SubConfiguration> m) {
            super.putAll(m);
        }
    }

    public enum Type {
        T1, T2;
    }

    @Data
    public static class SubItem {
        private String name;

        public SubItem(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return UnitSpec.toSString(this);
        }
    }
}
