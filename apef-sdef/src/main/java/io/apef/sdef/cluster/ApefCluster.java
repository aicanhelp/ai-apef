package io.apef.sdef.cluster;


public interface ApefCluster {
    String getConfig(String name, String defaultValue);

    void setConfig(String name, String value);

    void removeConfig(String name);
}
