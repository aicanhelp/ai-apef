package io.apef.base.config.factory;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class XFactoryConfigX<T extends XConfigBaseX<T>> extends XFactoryConfig<T> {

    private Map<String, XConfigBaseX.Extension<T>> extensions = new HashMap<>();

    public T config(String name) {
        if (StringUtils.isBlank(name)) return null;
        T config = this.get(name);
        if (config == null) {
            XConfigBaseX.Extension<T> extension = extensions.remove(name);
            if (extension != null) {
                config = extension.extension();
                if (config != null) {
                    super.put(config.getName(), config);
                    return config;
                }
            }
            throw new IllegalArgumentException("Can not find the config with name '" +
                    name + "' in " + this.getClass().getName());
        }
        return config;
    }

    private boolean containsName(String name) {
        return this.containsKey(name) || this.extensions.containsKey(name);
    }

    @Override
    public T put(String key, T value) {
        value.setName(key);
        value.setExtensionListener(extension -> {
            if(this.containsName(extension.getName())){
                throw new IllegalArgumentException("There are other same name configuration with this extension: " + extension.getName());
            }
            extensions.put(extension.getName(), extension);
        });
        return super.put(key, value);
    }
}
