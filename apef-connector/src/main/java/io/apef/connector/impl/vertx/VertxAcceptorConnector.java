package io.apef.connector.impl.vertx;

import io.apef.sdef.ApefSdef;

import io.apef.base.exception.VexException;
import io.apef.base.exception.VexExceptions;
import io.apef.connector.acceptor.AcceptorConnector;
import io.apef.sdef.connector.http.server.VertxHttpServer;
import io.apef.connector.acceptor.AcceptorRequestContext;
import io.apef.connector.base.TxInf;
import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VertxAcceptorConnector extends AcceptorConnector<RoutingContext,
        RoutingContext> {
    private VertxHttpServer httpServer;
    private VertxAcceptorChannelConfig acceptorConfig;

    public VertxAcceptorConnector(VertxAcceptorChannelConfig acceptorConfig) {
        super(acceptorConfig.getName());
        this.acceptorConfig = acceptorConfig;
        this.httpServer = ApefSdef.httpServer(acceptorConfig.getHttpServer());
        this.bindDispatcher();
    }

    public void bindMetrics() {
        this.httpServer.bindMetrics();
    }

    private void bindDispatcher() {
        httpServer.postHandler(this.acceptorConfig.getContext(), event -> {
            if (log.isDebugEnabled()) {
                log.debug("Accept Post Request: " + event.request().absoluteURI());
            }
            try {
                dispatch(event, event);
            } catch (Exception ex) {
                log.error("Failed to dispatch request", ex);
                event.response().setStatusCode(503).end();
            }
        });
        if (!this.acceptorConfig.isPostOnly()) {
            httpServer.getHandler(this.acceptorConfig.getContext(), event -> {
                if (log.isDebugEnabled()) {
                    log.debug("Accept Get Request: " + event.request().absoluteURI());
                }
                try {
                    dispatch(event, event);
                } catch (Exception ex) {
                    log.error("Failed to dispatch request", ex);
                    event.response().setStatusCode(503).end();
                }
            });
        }
    }

    @Override
    protected <T, R> AcceptorRequestContext<RoutingContext, RoutingContext, T, R> newRequestContext(RoutingContext inContext,
                                                                                                    RoutingContext outContext) {
        return new AcceptorRequestContext<RoutingContext, RoutingContext, T, R>(inContext, outContext) {
            @Override
            protected TxInf readTxInf() {
                String txInf = inContext.request().getHeader(HttpHeaders.xTX_INF.name());
                return TxInf.from(txInf);
            }

            @Override
            protected ByteBuf readRequestData() {
                return inContext.getBody().getByteBuf();
            }

            @Override
            protected void writeRxInf() {
                outContext.response()
                        .putHeader(HttpHeaders.xTX_INF.name(), this.txInf().toString());
            }

            @Override
            protected void writeResponseData(ByteBuf data) {
                if (outContext.response().ended()) {
                    log.warn("Http Response may handled by Http Server for Timeout, status: "
                            + outContext.response().getStatusMessage());
                    return;
                }
                if (response().statusCode() == 302) {
                    outContext.response()
                            .setStatusCode(302)
                            .putHeader("Location", response().errMsg());
                } else if (response().statusCode() != 200) {
                    VexException exception = VexExceptions.of(response().ex());
                    if (exception.getMessage() != null)
                        outContext.response()
                                .setStatusMessage(exception.getMessage());

                    outContext.response().setStatusCode(exception.httpStatusCode());
                }
                if (data == null) {
                    outContext.response().end();
                } else
                    outContext.response().end(Buffer.buffer(data));
            }
        };
    }
}
