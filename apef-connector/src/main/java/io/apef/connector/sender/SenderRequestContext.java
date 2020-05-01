package io.apef.connector.sender;

import io.apef.core.channel.pipe.ChannelPipeContext;
import io.apef.base.exception.VexExceptions;
import io.apef.connector.base.TxInf;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static io.apef.base.utils.Bytes.stringOf;

@Slf4j
@Getter
@Accessors(fluent = true)
public class SenderRequestContext<C extends ConnectorRequestContext<C>, K, T, R> extends SenderRequestMessage<C, K, T, R> {

    private SenderChannelContext<C, K, T, R> channelContext;
    private TxInf txInf;

    public SenderRequestContext(ChannelPipeContext channelPipeContext) {
        super(channelPipeContext);
    }

    public SenderRequestContext() {
    }

    public void attachSenderChannelContext(SenderChannelContext<C, K, T, R> channelContext) {
        this.channelContext = channelContext;
    }

    public ByteBuf affinityKey() {
        return this.channelContext.affinityKey(this.requestContent());
    }

    public TxInf txInf() {
        if (this.txInf != null) return this.txInf;
        this.txInf = TxInf.newTxInf(this.requestType().id());
        return this.txInf;
    }

    protected void sendRequest(ConnectorRequestContext connectorRequestContext) {
        ByteBuf requestData = null;
        if (this.requestContent() != null) {
            try {
                requestData = this.channelContext.encodeRequest(null, this.requestContent());
            } catch (Exception ex) {
                super.fail("failed to encodeRequestData:" + ex.getMessage(), VexExceptions.of(500));
                return;
            }
        }
        if (!this.txInf().isEmpty()) {
            try {
                connectorRequestContext.writeTxInf(this.txInf());
            } catch (Exception ex) {
                if (log.isDebugEnabled()) {
                    log.error("failed to write request TxInf", ex);
                }
                super.fail("failed to write request TxInf:" + ex.getMessage(), VexExceptions.of(500));
                return;
            }
        }
        try {
            connectorRequestContext.writeRequestData(requestData);
        } catch (Exception ex) {
            if (log.isDebugEnabled()) {
                log.error("failed to write request data", ex);
            }
            super.fail("failed to write request data:" + ex.getMessage(), VexExceptions.of(500));
        }
    }

    public void handleResponse(ConnectorRequestContext connectorRequestContext) {
        if (log.isDebugEnabled()) {
            log.debug("Handling response.");
        }
        if (!this.txInf.isEmpty()) {
            try {
                this.txInf = connectorRequestContext.readResponseTxInf();
            } catch (Exception ex) {
                if (log.isDebugEnabled()) {
                    log.error("failed to read Response TxInf", ex);
                }
                super.fail("failed to read Response TxInf:" + ex.getMessage(), VexExceptions.of(500));
                return;
            }
        }

        ByteBuf responseData = null;
        try {
            responseData = connectorRequestContext.readResponseData();
        } catch (Exception ex) {
            if (log.isDebugEnabled()) {
                log.error("failed to handle response Data", ex);
            }
            super.fail("failed to handle response Data:" + ex.getMessage(), VexExceptions.of(500));
            return;
        }

        R response = null;
        if (responseData != null) {
            if (this.txInf.success()) {
                try {
                    response = this.channelContext.decodeResponse(null,
                            this.requestContent(), responseData);
                } catch (Exception ex) {
                    if (log.isDebugEnabled()) {
                        log.error("failed to decodeResponse", ex);
                    }
                    super.fail("failed to decodeResponse", VexExceptions.of(500));
                    return;
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Finish handling response.");
        }
        if (txInf.success())
            super.succeed(response);
        else
            super.fail("" + stringOf(responseData), VexExceptions.of(txInf.statusCode()));
    }

}
