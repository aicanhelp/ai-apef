package io.apef.connector.acceptor;

import io.apef.connector.base.ConnectorReader;

public abstract class AcceptorReader<IC> extends ConnectorReader<IC> {
    protected AcceptorReader(IC inContext) {
        super(inContext);
    }
}
