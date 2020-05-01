package io.apef.metrics.api;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor
public enum MetricsApiParam {
    action, metric, type, item;

    public String pathContext(String root) {
        StringBuilder path = rootPath(root);
        path.append(MetricsApiParam.action);
        for (int i = 1; i < this.ordinal() + 1; i++) {
            path.append("/:").append(MetricsApiParam.values()[i]);
        }

        return path.toString();
    }

    private StringBuilder rootPath(String root) {
        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.isEmpty(root)) return stringBuilder.append(":");
        if (root.charAt(root.length() - 1) == '/') return stringBuilder.append(root).append(":");
        return stringBuilder.append(root).append("/:");
    }

    public String pathContext() {
        return this.pathContext(null);
    }

    public static String metricsPathContext(String root) {
        return item.pathContext(root);
    }
}
