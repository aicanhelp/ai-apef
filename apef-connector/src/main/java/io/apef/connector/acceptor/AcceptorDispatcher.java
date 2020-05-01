package io.apef.connector.acceptor;

import io.apef.base.exception.VexExceptions;
import io.apef.connector.base.ConnectorChannelContextManager;
import lombok.extern.slf4j.Slf4j;

/**
 * @param <IC> InStream Context
 * @param <OC> OutStream Context
 */
@Slf4j
public class AcceptorDispatcher<IC, OC> {
    private final static String DISPATCH_FAILURE = "Internal Error, Failed to dispatch request, " +
            "No Registered Context Found for requestType:";
    private final static String INVALID_TX_INF = "Invalid: failed to get the transaction inf";

    private ConnectorChannelContextManager<AcceptorChannelContext>
            contextManager;

    public AcceptorDispatcher(String name) {
        this.contextManager = new ConnectorChannelContextManager<>(
                name + "_AcceptorChannelContextManager",
                AcceptorChannelContext[]::new
        );
    }

    public void dispatch(AcceptorRequestContext<IC, OC, ?, ?> requestContext) {
        requestContext.onTxInfRead(rc -> {
            if (!requestContext.txInf().isValid()) {
                requestContext.response().fail(500,
                        INVALID_TX_INF, VexExceptions.E_500.exception());
                return;
            }

            AcceptorChannelContext channelContext;
            if (this.contextManager.byPass()) {
                channelContext = this.contextManager.defaultContext();
            } else {
                channelContext = this.contextManager.channelContext(requestContext.txInf().requestType());
            }

            if (channelContext == null) {
                log.error(DISPATCH_FAILURE + requestContext.txInf().requestType());
                requestContext.response().fail(500,
                        DISPATCH_FAILURE, VexExceptions.E_500.exception());
                return;
            }

            requestContext
                    .attachChannelContext(channelContext)
                    .onRequestDataRead(rc2 -> {
                        channelContext.accept(requestContext);
                    });

        }).readRequest();
    }

    public void registerChannelContext(AcceptorChannelContext channelContext) {
        this.contextManager.registerChannelContext(channelContext);
    }
}
