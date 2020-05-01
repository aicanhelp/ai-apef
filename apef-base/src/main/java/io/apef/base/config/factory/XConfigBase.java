package io.apef.base.config.factory;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public abstract class XConfigBase extends XEnabledConfig implements IConfigBase {
    private String name;

    protected XConfigBase() {

    }

    protected XConfigBase(String name) {
        this.name = name;
    }

}
