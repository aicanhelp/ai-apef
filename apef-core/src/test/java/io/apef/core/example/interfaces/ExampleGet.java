package io.apef.core.example.interfaces;

import io.apef.core.channel.impl.ChannelMessageImpl;
import io.apef.core.channel.pipe.ChannelPipeContext;
import io.apef.core.channel.request.S2BRequest;

public interface ExampleGet<M extends ExampleGet<M>> extends S2BRequest<M, String, String> {
    ExampleGet<M> dataKey(String key);

    static <M extends ExampleGet<M>> M create(ChannelPipeContext channelPipeContext) {
        return (M) new ExampleGetMessage(channelPipeContext);
    }
}

class ExampleGetMessage<M extends ExampleGetMessage<M>>
        extends ChannelMessageImpl<M, String, String>
        implements ExampleGet<M> {
    protected ExampleGetMessage(ChannelPipeContext channelPipeContext) {
        super(channelPipeContext);
        this.messageType(ExampleMessageType.GET);
    }

    @Override
    public ExampleGet<M> dataKey(String key) {
        return this.requestContent(key);
    }
}
