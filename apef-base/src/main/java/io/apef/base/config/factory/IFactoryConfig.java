package io.apef.base.config.factory;


public interface IFactoryConfig<T extends IConfigBase> {
    T config(String name);
}
