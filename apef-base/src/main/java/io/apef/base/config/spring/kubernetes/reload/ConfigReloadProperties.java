package io.apef.base.config.spring.kubernetes.reload;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ConfigReloadProperties {

    /**
     * Enables the Kubernetes configuration reload on change.
     */
    private boolean enabled = false;

    /**
     * Enables monitoring on config maps to detect changes.
     */
    private boolean monitoringConfigMaps = true;

    /**
     * Enables monitoring on secrets to detect changes.
     */
    private boolean monitoringSecrets = false;

    /**
     * Sets the detection mode for Kubernetes configuration reload.
     */
    private ReloadDetectionMode mode = ReloadDetectionMode.EVENT;

    /**
     * Sets the polling period in milliseconds to use when the detection mode is POLLING.
     */
    private Long period = 15000L;

    public ConfigReloadProperties() {
    }

    public enum ReloadDetectionMode {
        /**
         * Enables a polling task that retrieves periodically all external properties and
         * fire a reload when they change.
         */
        POLLING,

        /**
         * Listens to Kubernetes events and checks if a reload is needed when configmaps or secrets change.
         */
        EVENT
    }
}
