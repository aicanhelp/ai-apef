package io.apef.base.config.spring.kubernetes;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public abstract class AbstractConfigProperties {

    protected boolean enabled = true;
    protected String name;
    protected String namespace;

    public abstract String getConfigurationTarget();

}
