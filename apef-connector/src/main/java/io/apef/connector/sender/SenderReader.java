package io.apef.connector.sender;

import io.apef.connector.base.ConnectorReader;

public abstract class SenderReader<IC> extends ConnectorReader<IC> {

    protected SenderReader(IC inContext) {
        super(inContext);
    }
}
