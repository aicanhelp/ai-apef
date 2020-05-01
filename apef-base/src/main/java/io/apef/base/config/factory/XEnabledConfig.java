package io.apef.base.config.factory;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public abstract class XEnabledConfig implements IEnabledConfig {
    private boolean enabled = true;
}
