package io.apef.connector.sender;

import io.apef.core.channel.MessageType;
import io.apef.core.channel.impl.ChannelMessageImpl;
import io.apef.core.channel.pipe.ChannelPipeContext;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.function.Supplier;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class SenderRequestMessage<C extends ConnectorRequestContext<C>, K, T, R>
        extends ChannelMessageImpl<SenderRequestMessage<C, K, T, R>, T, R>
        implements SenderRequest<SenderRequestMessage<C, K, T, R>, T, R> {
    public final static MessageType TYPE = MessageType.newType();

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    @Accessors(fluent = true)
    private boolean hitCache;
    private boolean ignoreCache;

    private MessageType requestType = MessageType.NO_TYPE;

    private SenderChannelContext<C, K, T, R> channelContext;

    protected SenderRequestMessage(ChannelPipeContext channelPipeContext) {
        super(channelPipeContext);
        super.messageType(TYPE);
    }

    public void attachSenderChannelContext(SenderChannelContext<C, K, T, R> channelContext) {
        this.channelContext = channelContext;
    }

    protected SenderRequestMessage() {

    }

    public static <T, R> SenderRequest<?, T, R> mockRequest(Supplier<R> responseSupplicer) {
        return new SenderRequestMessage() {
            @Override
            public void end() {
                end(responseSupplicer);
            }
        };
    }
}
