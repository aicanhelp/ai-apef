package io.apef.base.config.utils;

import io.apef.base.config.spring.XmlPropertySourceLoader;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.beans.PropertyEditor;
import java.util.*;

public class ConfigurationLoader {
    private static Logger log = LoggerFactory.getLogger(ConfigurationLoader.class);
    public final static String CONFIG_ENV_PREFIX = "CONFIG_ENV_PREFIX";

    private final static YamlPropertySourceLoader yamlPropertySourceLoader = new YamlPropertySourceLoader();
    private final static PropertiesPropertySourceLoader propertiesPropertySourceLoader = new PropertiesPropertySourceLoader();
    private final static XmlPropertySourceLoader xmlPropertySourceLoader = new XmlPropertySourceLoader();

    public static boolean exists(String confFile) {
        ClassPathResource classPathResource = new ClassPathResource(confFile);
        return classPathResource.exists();
    }

    private static MutablePropertySources propertySources(boolean warningNotFound, String... confFiles) {
        MutablePropertySources propertySources = new MutablePropertySources();
        List<String> notFoundFiles = new ArrayList<>();
        for (String confFile : confFiles) {
            ClassPathResource classPathResource = new ClassPathResource(confFile);
            if (!classPathResource.exists()) {
                notFoundFiles.add(confFile);
                continue;
            }

            try {
                List<PropertySource<?>> propertySource = propertySourceLoader(confFile)
                        .load(confFile, classPathResource);
                if (propertySource != null)
                    propertySources.addFirst(propertySource.get(0));
            } catch (Exception ex) {
                log.error("Failed to load configuration file: " + confFile, ex);
            }
        }

        //if all config files can not be loaded, the warning must be print
        if (notFoundFiles.size() == confFiles.length && warningNotFound) {
            log.warn("No configuration file in classpath for file name: {}", notFoundFiles);
        }

        return propertySources;
    }

    private static MutablePropertySources propertySources(String... confFiles) {
        return propertySources(true, confFiles);
    }

    private static PropertySourceLoader propertySourceLoader(String confFile) {
        if (StringUtils.endsWithIgnoreCase(confFile, ".xml"))
            return xmlPropertySourceLoader;
        if (StringUtils.endsWithIgnoreCase(confFile, ".yml"))
            return yamlPropertySourceLoader;
        if (StringUtils.endsWithIgnoreCase(confFile, ".yaml"))
            return yamlPropertySourceLoader;
        return propertiesPropertySourceLoader;
    }

    public static <T> T loadConfiguration(Class<T> tClass, boolean warningNotFound, Map<Class, PropertyEditor> propertyEditors,
                                          List<String> envPropertyPrefix,
                                          String... confFiles) {
        if (confFiles == null) return null;
        Object target = null;
        try {
//            target = tClass.newInstance();
//            CustomPropertiesConfigurationFactory<Object> factory = new CustomPropertiesConfigurationFactory<>(
//                    target, propertyEditors);
//
//            factory.setIgnoreInvalidFields(false);
//            factory.setIgnoreUnknownFields(true);
//            factory.setExceptionIfInvalid(true);
            MutablePropertySources propertySources = propertySources(warningNotFound, confFiles);

            if (envPropertyPrefix == null || envPropertyPrefix.isEmpty()) {
                String envPrefixString = System.getProperty(CONFIG_ENV_PREFIX);
                if (!StringUtils.isEmpty(envPrefixString)) {
                    envPropertyPrefix = Arrays.asList(envPrefixString.split(","));
                }
            }

            if (envPropertyPrefix != null && !envPropertyPrefix.isEmpty()) {
                Map<String, Object> envProperties = new HashMap<>();
                envPropertyPrefix.forEach(s -> {
                    System.getProperties().forEach((o, o2) -> {
                        if (((String) o).startsWith(s)) {
                            envProperties.put((String) o, o2);
                        }
                    });
                });
                MapPropertySource envPropertySource = new MapPropertySource("System_Properties",
                        envProperties);
                propertySources.addFirst(envPropertySource);
            } else {
                propertySources.addFirst(new MapPropertySource("System_Properties", (Map) System.getProperties()));
            }

            propertySources.addFirst(new MapPropertySource("System_Env", (Map) System.getenv()));

            Binder binder = new Binder(ConfigurationPropertySources.from(IteratorUtils.asIterable(propertySources.iterator())));
            return binder.bind("propertyclass", tClass).get();

//            factory.setPropertySources(propertySources);
//            factory.bindPropertiesToTarget();
        } catch (Exception ex) {
            log.error("Failed to load configuration files: {}", confFiles, ex);
            return null;
        }

//        return (T) target;
    }

    public static <T> T loadConfiguration(Class<T> tClass, Map<Class, PropertyEditor> propertyEditors,
                                          List<String> envPropertyPrefix,
                                          String... confFiles) {
        return loadConfiguration(tClass, false, propertyEditors, envPropertyPrefix, confFiles);
    }

    public static <T> T loadConfiguration(Class<T> tClass, Map<Class, PropertyEditor> propertyEditors, String... confFiles) {
        return loadConfiguration(tClass, false, propertyEditors, null, confFiles);
    }

    public static <T> T loadConfiguration(Class<T> tClass, boolean warningNotFound, String... confFiles) {
        return loadConfiguration(tClass, warningNotFound, null, null, confFiles);
    }

    public static <T> T loadConfiguration(Class<T> tClass, String... confFiles) {
        return loadConfiguration(tClass, false, null, null, confFiles);
    }
}
