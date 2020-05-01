package io.apef.connector.acceptor;

import io.apef.connector.base.TxInf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

import static io.apef.base.utils.Bytes.bytesOf;

/**
 * @param <IC> inStream Context
 * @param <OC> outStream Context
 * @param <T>  Request
 * @param <R>  Response
 */

@Slf4j
public abstract class AcceptorRequestContext<IC, OC, T, R> {
    private final static byte[] ENCODER_FAILURE = bytesOf("Internal Error, Failed to encode Response");

    private IC inContext;
    private OC outContext;

    private AcceptorChannelContext<IC, OC, T, R> channelContext;

    @Getter
    @Accessors(fluent = true)
    private TxInf txInf;

    @Getter
    @Accessors(fluent = true)
    private T request;

    @Getter
    @Accessors(fluent = true)
    private AcceptorResponse<R> response;

    @Setter(AccessLevel.PROTECTED)
    @Accessors(fluent = true)
    private Consumer<AcceptorRequestContext<IC, OC, T, R>> onTxInfRead;

    protected AcceptorRequestContext(IC inContext, OC outContext) {
        this.inContext = inContext;
        this.outContext = outContext;
    }

    protected AcceptorRequestContext<IC, OC, T, R> attachChannelContext(AcceptorChannelContext<IC, OC, T, R> channelContext) {
        this.channelContext = channelContext;
        return this;
    }

    protected void onRequestDataRead(Consumer<AcceptorRequestContext<IC, OC, T, R>> onDataRead) {
        this.response = new AcceptorResponse<>(this);
        ByteBuf data = this.readRequestData();

        if (data != null) {
            try {
                this.request = channelContext.decodeRequest(this.inContext, data);
            } catch (Exception ex) {
                log.error("Failed to decode request", ex);
                this.response.fail("Failed on build request from RequestContext", ex);
            }
        }

        onDataRead.accept(this);
    }

    protected void readRequest() {
        this.txInf = this.readTxInf();
        this.onTxInfRead.accept(this);
    }

    public boolean isEnd() {
        return this.response.end();
    }

    protected abstract TxInf readTxInf();

    protected abstract ByteBuf readRequestData();

    protected abstract void writeRxInf();

    protected abstract void writeResponseData(ByteBuf data);

    protected void writeResponse() {
        if (this.isEnd()) return;
        if (log.isDebugEnabled()) {
            log.debug("Try to write response for request:{}, Response Data:" + this.response,
                    this.request);
        }

        if (!this.txInf.isEmpty()) {
            this.txInf.success(this.response.success())
                    .statusCode((short) this.response.statusCode());
        }

        ByteBuf responseData = null;
        try {
            if (this.response.success()) {
                responseData = this.channelContext.encodeResponse(this.outContext,
                        this.response);
            } else {
                if (response.errMsg() != null)
                    responseData = Unpooled.wrappedBuffer(bytesOf(response.errMsg()));
            }
        } catch (Exception ex) {
            log.warn("Failed to encode response:" + ex.getMessage());
            if (!this.txInf.isEmpty())
                this.txInf.success(false, (short) 500);
            responseData = Unpooled.wrappedBuffer(ENCODER_FAILURE);
        }

        if (!this.txInf.isEmpty()) {
            this.writeRxInf();
        }

        this.writeResponseData(responseData);

        this.channelContext.finish(this);

        if (log.isDebugEnabled()) {
            log.debug("Finished wirte the response.");
        }
    }
}
