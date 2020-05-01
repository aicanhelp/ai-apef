package io.apef.base.config.factory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class XFactoryConfig<T extends IConfigBase> extends HashMap<String, T>
        implements IFactoryConfig<T> {
    protected final static Logger log = LoggerFactory.getLogger(XFactoryConfig.class);

    public XFactoryConfig() {
    }

    public T config(String name) {
        if (StringUtils.isBlank(name)) return null;
        T config = this.get(name);
        if (config == null) {
            throw new IllegalArgumentException("Can not find the config with name '" +
                    name + "' in " + this.getClass().getName());
        }
        return config;
    }

    public void enableConfig(String name) {
        if (StringUtils.isBlank(name)) return;
        T config = this.get(name);
        if (config != null) config.setEnabled(true);
    }

    public void disableConfig(String name) {
        if (StringUtils.isBlank(name)) return;
        T config = this.get(name);
        if (config != null) config.setEnabled(false);
    }

    public void add(T config) {
        if (config == null) {
            throw new IllegalArgumentException("Can not add a null config");
        }

        if (StringUtils.isBlank(config.getName())) {
            throw new IllegalArgumentException("Can not add a config with blank name");
        }

        this.put(config.getName(), config);
    }

    private boolean containsName(String name) {
        return this.containsKey(name);
    }

    @Override
    public T put(String key, T value) {
        value.setName(key);
        if (this.containsKey(key)) {
            throw new IllegalArgumentException("Can not set two configs with the same name: " + value.getName());
        }
        return super.put(key, value);
    }

    @Override
    public T putIfAbsent(String key, T value) {
        value.setName(key);
        if (this.containsName(value.getName())) return value;
        this.putIfAbsent(value.getName(), value);
        return super.putIfAbsent(key, value);
    }

    public List<T> allEnabled() {
        return this.values().stream().filter(IEnabledConfig::isEnabled).collect(Collectors.toList());
    }
}
