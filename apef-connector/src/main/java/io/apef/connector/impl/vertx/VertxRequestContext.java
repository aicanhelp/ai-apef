package io.apef.connector.impl.vertx;

import io.apef.base.exception.VexExceptions;
import io.apef.connector.base.TxInf;
import io.apef.connector.sender.ConnectorRequestContext;
import io.apef.connector.sender.SenderRequestContext;
import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import static io.apef.base.utils.Bytes.byteBufOf;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(fluent = true)
public class VertxRequestContext extends ConnectorRequestContext<VertxRequestContext> {
    private HttpClientRequest request;
    private HttpClientResponse response;
    private ByteBuf body;

    public VertxRequestContext(HttpClientRequest clientRequest,
                               SenderRequestContext<VertxRequestContext, ?, ?, ?> requestContext) {
        super(requestContext);
        this.request = clientRequest;
        clientRequest.handler(event -> {
            event.bodyHandler(buffer -> {
                handleResponse(event, buffer.getByteBuf());
            }).exceptionHandler(e -> {
                requestContext.fail("Failed on send get response body:" + e.getMessage(), VexExceptions.of(e));
            });
        }).exceptionHandler(e -> {
            requestContext.fail("Failed on send Http request:" + e.getMessage(), VexExceptions.of(e));
        });
    }

    protected void handleResponse(HttpClientResponse response, ByteBuf body) {
        this.response = response;
        this.body = body;
        this.requestContext().handleResponse(this);
    }

    @Override
    protected void writeTxInf(TxInf txInf) throws Exception {
        request.putHeader(HttpHeaders.xTX_INF.name(),
                String.valueOf(txInf.toString()));
    }

    @Override
    protected void writeRequestData(ByteBuf data) throws Exception {
        if (data == null)
            request.end();
        else
            request.end(Buffer.buffer(data));
    }

    @Override
    protected TxInf readResponseTxInf() throws Exception {
        TxInf txInf = TxInf.from(response
                .getHeader(HttpHeaders.xTX_INF.name()));
        if (txInf.isEmpty()) {
            txInf.statusCode((short) response.statusCode());
            txInf.success(response.statusCode() == 200);
        }
        return txInf;
    }

    @Override
    protected ByteBuf readResponseData() throws Exception {
        switch (response.statusCode()) {
            case 302:
                return byteBufOf(response.getHeader("Location"));
            default:
                return this.body;
        }
    }
}