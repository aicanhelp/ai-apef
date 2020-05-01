package io.apef.connector.base;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public abstract class ConnectorWriter<OC> {
    private OC outContext;

    protected ConnectorWriter(OC outContext) {
        this.outContext = outContext;
    }

}
