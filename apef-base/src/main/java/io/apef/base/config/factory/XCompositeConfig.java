package io.apef.base.config.factory;

import io.apef.base.config.utils.ConfigUtils;

import java.util.List;

public abstract class XCompositeConfig {

    public <T> List<T> configs(Class<T> fieldClass) {
        return ConfigUtils.configs(fieldClass, this, false);
    }

    public <T> List<T> configs(Class<T> fieldClass, boolean includeNull) {
        return ConfigUtils.configs(fieldClass, this, includeNull);
    }
}
