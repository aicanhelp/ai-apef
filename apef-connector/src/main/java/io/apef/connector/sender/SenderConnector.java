package io.apef.connector.sender;


public abstract class SenderConnector<C extends ConnectorRequestContext<C>> {
    public void accept(SenderRequestContext<C, ?, ?, ?> requestContext) {
        this.newConnectorContext(requestContext).start();
    }

    protected abstract C newConnectorContext(SenderRequestContext<C, ?, ?, ?> requestContext);
}
