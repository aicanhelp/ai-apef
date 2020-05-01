package io.apef.base.config.spring.kubernetes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

public class SecretsPropertySource extends MapPropertySource {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecretsPropertySource.class);

    static final String PROPERTY_SOURCE_NAME_SEPARATOR = ".";
    private static final String PREFIX = "secrets";

    public SecretsPropertySource(KubernetesClient client, SecretsConfigProperties config) {
        super(
                getSourceName(client, config),
                getSourceData(client, config)
        );
    }

    private static String getSourceName(KubernetesClient client, SecretsConfigProperties config) {
        return new StringBuilder()
                .append(PREFIX)
                .append(PROPERTY_SOURCE_NAME_SEPARATOR)
                .append(config.getName())
                .append(PROPERTY_SOURCE_NAME_SEPARATOR)
                .append(config.getNamespace())
                .toString();
    }

    private static Map<String, Object> getSourceData(KubernetesClient client, SecretsConfigProperties config) {
        String name = config.getName();
        String namespace = config.getNamespace();
        Map<String, Object> result = new HashMap<>();

        if (config.isEnableApi()) {
            try {
                // Read for secrets api (named)
                if (StringUtils.isEmpty(namespace)) {
                    putAll(
                            client.secrets()
                                    .withName(name)
                                    .get(),
                            result);
                } else {
                    putAll(
                            client.secrets()
                                    .inNamespace(namespace)
                                    .withName(name)
                                    .get(),
                            result);
                }

                // Read for secrets api (label)
                if (!config.getLabels().isEmpty()) {
                    if (StringUtils.isEmpty(namespace)) {
                        client.secrets()
                                .withLabels(config.getLabels())
                                .list()
                                .getItems()
                                .forEach(s -> putAll(s, result));
                    } else {
                        client.secrets()
                                .inNamespace(namespace)
                                .withLabels(config.getLabels())
                                .list()
                                .getItems()
                                .forEach(s -> putAll(s, result));
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Can't read secret with name: [{}] or labels [{}] in namespace:[{}] (cause: {}). Ignoring",
                        name,
                        config.getLabels(),
                        namespace,
                        e.getMessage());
            }
        }

        // read for secrets mount
        config.getPaths()
                .stream()
                .map(Paths::get)
                .filter(Files::exists)
                .forEach(p -> putAll(p, result));

        return result;
    }

    // *****************************
    // Helpers
    // *****************************
    private static void putAll(Secret secret, Map<String, Object> result) {
        if (secret != null && secret.getData() != null) {
            secret.getData().forEach((k, v) -> result.put(
                    k,
                    new String(Base64.getDecoder().decode(v)).trim())
            );
        }
    }

    private static void putAll(Path path, Map<String, Object> result) {
        try {
            Files.walk(path)
                    .filter(Files::isRegularFile)
                    .forEach(p -> readFile(p, result));
        } catch (IOException e) {
            LOGGER.warn("", e);
        }
    }

    private static void readFile(Path path, Map<String, Object> result) {
        try {
            result.put(
                    path.getFileName().toString(),
                    new String(Files.readAllBytes(path)).trim());
        } catch (IOException e) {
            LOGGER.warn("", e);
        }
    }
}
