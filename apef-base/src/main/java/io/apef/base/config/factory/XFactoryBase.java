package io.apef.base.config.factory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class XFactoryBase<T,
        C extends IConfigBase> {
    protected final static Logger log = LoggerFactory.getLogger(XFactoryBase.class);
    protected XFactoryConfig<C> factoryConfig;
    protected Map<String, T> instances = new ConcurrentHashMap<>();

    public XFactoryBase(XFactoryConfig<C> factoryConfig) {
        this.factoryConfig = factoryConfig;
    }

    public synchronized T instance(String name) {
        if (StringUtils.isBlank(name)) {
            throw new RuntimeException("Try to get a instance with a blank name");
        }
        T instance = this.instances.get(name);
        if (instance != null) return instance;
        C config = this.factoryConfig.config(name);
        try {
            if (config == null) {
                throw new IllegalArgumentException("Can not find config with name: " + name);
            }
            instance = this.newInstance(config);
            this.instances.put(name, instance);
            return instance;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to create instance with configuration: "
                    + config
                    , ex);
        }
    }

    public List<T> allEnabled() {
        return this.factoryConfig.allEnabled().stream()
                .map(c -> this.instance(c.getName()))
                .collect(Collectors.toList());
    }

    protected abstract T newInstance(C config) throws Exception;

    public void close(String name) {
        T instance = this.instances.remove(name);
        if (instance != null) {
            this.close(instance);
        }
    }

    protected abstract void close(T instance);

    public void closeAll() {
        this.allEnabled().forEach(this::close);
    }
}
