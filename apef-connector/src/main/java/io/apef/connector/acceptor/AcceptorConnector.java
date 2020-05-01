package io.apef.connector.acceptor;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @param <IC> InStream Context
 * @param <OC> OutStream Context
 */
@Data
@Slf4j
public abstract class AcceptorConnector<IC, OC> {

    private AcceptorDispatcher<IC, OC> dispatcher;

    protected AcceptorConnector(String name) {
        this.dispatcher = new AcceptorDispatcher<>(name + "_dispatcher");
    }

    protected void dispatch(IC inContext, OC outContext) {
        AcceptorRequestContext<IC, OC, ?, ?> requestContext = newRequestContext(inContext, outContext);

        try {
            this.dispatcher.dispatch(requestContext);
        } catch (Exception ex) {
            log.error("Failed to dispatch acceptor request", ex);
            requestContext.response().fail(ex);
        }
    }

    protected abstract <T, R> AcceptorRequestContext<IC, OC, T, R> newRequestContext(IC inContext, OC outContext);

}
