package io.apef.base.config.spring.kubernetes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.apef.base.config.utils.Xml2Properties;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.ByteArrayResource;

public class ConfigMapPropertySource extends MapPropertySource {
    static final String PROPERTY_SOURCE_NAME_SEPARATOR = ".";
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigMapPropertySource.class);

    private static final String YML = ".yml";
    private static final String YAML = ".yaml";
    private static final String XML = ".xml";
    private static final String PROPERTIES = ".properties";

    private static final String PREFIX = "configmap";

    public ConfigMapPropertySource(KubernetesClient client, String name) {
        this(client, name, null);
    }

    public ConfigMapPropertySource(KubernetesClient client,
                                   String name, String namespace) {
        super(getName(client, name, namespace), asObjectMap(getData(client, name, namespace)));
    }

    private static String getName(KubernetesClient client, String name, String namespace) {
        return new StringBuilder()
                .append(PREFIX)
                .append(PROPERTY_SOURCE_NAME_SEPARATOR)
                .append(name)
                .append(PROPERTY_SOURCE_NAME_SEPARATOR)
                .append(namespace == null || namespace.isEmpty() ? client.getNamespace() : namespace)
                .toString();
    }

    private static Map<String, String> getData(KubernetesClient client, String name, String namespace) {
        Map<String, String> result = new HashMap<>();
        try {
            ConfigMap map = namespace == null || namespace.isEmpty()
                    ? client.configMaps().withName(name).get()
                    : client.configMaps().inNamespace(namespace).withName(name).get();

            if (map != null) {
                for (Map.Entry<String, String> entry : map.getData().entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    Function<String, Properties> converter = null;
                    switch (key.toLowerCase()) {
                        case XML:
                            converter = XML_TO_PROPETIES;
                            break;
                        case YML:
                        case YAML:
                            converter = YAML_TO_PROPETIES;
                            break;
                        case PROPERTIES:
                            converter = KEY_VALUE_TO_PROPERTIES;
                    }

                    if (converter != null) {
                        result.putAll(converter.andThen(PROPERTIES_TO_MAP).apply(value));
                    } else {
                        result.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Can't read configMap with name: [" + name + "] in namespace:[" + namespace + "]. Ignoring");
        }
        return result;
    }

    private static Map<String, Object> asObjectMap(Map<String, String> source) {
        return source.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static final Function<String, Properties> YAML_TO_PROPETIES = s -> {
        YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
        yamlFactory.setResources(new ByteArrayResource(s.getBytes()));
        return yamlFactory.getObject();
    };

    private static final Function<String, Properties> XML_TO_PROPETIES = s -> {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(s.getBytes());
        try {
            return Xml2Properties.load(inputStream);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    };

    private static final Function<String, Properties> KEY_VALUE_TO_PROPERTIES = s -> {
        Properties properties = new Properties();
        try {
            properties.load(new ByteArrayInputStream(s.getBytes()));
            return properties;
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
    };

    private static final Function<Properties, Map<String, String>> PROPERTIES_TO_MAP = p -> p.entrySet().stream()
            .collect(Collectors.toMap(
                    e -> String.valueOf(e.getKey()),
                    e -> String.valueOf(e.getValue())));


}
