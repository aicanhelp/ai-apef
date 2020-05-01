package io.apef.connector.acceptor;

import io.apef.core.channel.MessageType;
import io.apef.base.exception.VexExceptions;
import io.apef.connector.base.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.*;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;

import static io.apef.base.utils.Bytes.bytesOf;

/**
 * Generally, there are three type of ChannelContext:
 * (1) ByPass ChannelContext:
 * Request does not include RequestType information
 * and Response does not include additional information, such as succeed or failure
 * (2) No_Type ChannelContext:
 * Request does not include the requestType information,
 * BUT Response  does include some additional information, such as succeed or failure information
 * (3) Typed ChannelContext:
 * Request does include RequestType information
 * AND Response  does include some additional information, such as succeed or failure
 *
 * @param <IC> InStream Context
 * @param <OC> OutStream Context
 * @param <T>  RequestData Type
 * @param <R>  ResponseData Type
 */

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AcceptorChannelContext<IC, OC, T, R> implements ConnectorChannelContext {
    private final static String HANDLER_FAILURE = "Internal Error, Exception thrown in handling request";
    private final static String REJECT_FAILURE = "Internal Error, more than max capability";
    private final static String ENCODER_FAILURE = "Internal Error, failed on encoding response";

    @Wither
    private MessageType requestType = MessageType.NO_TYPE;

    @Wither
    private MaxAccepted maxAccepted = new MaxAccepted(100000);

    @Wither
    private RequestDecoder<IC, T> requestDecoder;
    @Wither
    private ResponseEncoder<OC, R> responseEncoder;
    @Wither
    private AcceptorHandler<IC, OC, T, R> requestHandler;

    private AcceptorChannel<IC, OC> acceptorChannel;

    AcceptorChannelContext(AcceptorChannel acceptorChannel) {
        this.acceptorChannel = acceptorChannel;
    }

    public MessageType requestType() {
        return this.requestType;
    }

    public AcceptorChannel<IC, OC> endChannelContext() {
        check(this);
        this.acceptorChannel.registerChannelContext(this);

        return this.acceptorChannel;
    }

    public ByteBuf encodeResponse(OC outContext, AcceptorResponse<R> response) throws Exception {
        ByteBuf byteBuf = Unpooled.buffer();

        try {
            this.responseEncoder.encode(outContext, byteBuf, response.response());
        } catch (Exception ex) {
            byteBuf.clear();
            return byteBuf.writeBytes(bytesOf(ENCODER_FAILURE));
        }

        return byteBuf;
    }

    public T decodeRequest(IC inContext, ByteBuf data) throws Exception {
        return this.requestDecoder.decode(inContext, data);
    }

    public void accept(AcceptorRequestContext<IC, OC, T, R> requestContext) {
        if (!this.maxAccepted.acceptNext()) {
            log.warn("Over load, current Accepted more than " + this.maxAccepted.maxAccepted());
            requestContext.response().fail(503, REJECT_FAILURE,
                    VexExceptions.E_503.exception());
            return;
        }

        if (requestContext.isEnd()) return;
        try {
            this.requestHandler.handle(requestContext);
        } catch (Exception ex) {
            if (log.isDebugEnabled())
                log.error("Failed to handle request:" + requestType(), ex);
            requestContext.response().fail(500, HANDLER_FAILURE,
                    VexExceptions.E_500.exception());
        }
    }

    public void finish(AcceptorRequestContext<IC, OC, T, R> requestContext) {
        this.maxAccepted.down();
    }
}
