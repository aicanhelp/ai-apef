package io.apef.base.config.spring.kubernetes.reload;


import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class EventBasedConfigWatcher {
    private ConfigReloadProperties properties;
    private KubernetesClient kubernetesClient;

    private Map<String, Watch> watches;

    public EventBasedConfigWatcher(ConfigReloadProperties properties,
                                   KubernetesClient kubernetesClient) {
        this.properties = properties;
        this.kubernetesClient = kubernetesClient;
        this.watches = new HashMap<>();
    }

    public void watch() {
        boolean activated = false;

        if (properties.isMonitoringConfigMaps()) {
            try {
                String name = "config-maps-watch";
                watches.put(name, kubernetesClient.configMaps()
                        .watch(new Watcher<ConfigMap>() {
                            @Override
                            public void eventReceived(Watcher.Action action, ConfigMap configMap) {
                                onEvent(configMap);
                            }

                            @Override
                            public void onClose(KubernetesClientException e) {
                            }
                        }));
                activated = true;
                log.info("Added new Kubernetes watch: {}", name);
            } catch (Exception e) {
                log.error("Error while establishing a connection to watch config maps: configuration may remain stale", e);
            }
        }

        if (properties.isMonitoringSecrets()) {
            try {
                activated = false;
                String name = "secrets-watch";
                watches.put(name, kubernetesClient.secrets()
                        .watch(new Watcher<Secret>() {
                            @Override
                            public void eventReceived(Action action, Secret secret) {
                                onEvent(secret);
                            }

                            @Override
                            public void onClose(KubernetesClientException e) {
                            }
                        }));
                activated = true;
                log.info("Added new Kubernetes watch: {}", name);
            } catch (Exception e) {
                log.error("Error while establishing a connection to watch secrets: configuration may remain stale", e);
            }
        }

        if (activated) {
            log.info("Kubernetes event-based configuration change detector activated");
        }
    }

    public void unwatch() {
        if (this.watches != null) {
            for (Map.Entry<String, Watch> entry : this.watches.entrySet()) {
                try {
                    log.debug("Closing the watch {}", entry.getKey());
                    entry.getValue().close();

                } catch (Exception e) {
                    log.error("Error while closing the watch connection", e);
                }
            }
        }
    }

    private void onEvent(ConfigMap configMap) {
    }

    private void onEvent(Secret secret) {
    }
}
