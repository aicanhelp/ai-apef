package io.apef.base.config.factory;

public interface IConfigBase extends IEnabledConfig {
    String getName();

    IConfigBase setName(String name);
}
