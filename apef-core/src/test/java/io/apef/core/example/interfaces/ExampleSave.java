package io.apef.core.example.interfaces;

import io.apef.core.channel.impl.ChannelMessageImpl;
import io.apef.core.channel.pipe.ChannelPipeContext;
import io.apef.core.channel.request.B2CRequest;

public interface ExampleSave<M extends ExampleSave<M>>
        extends B2CRequest<M, String, Boolean> {
    ExampleSave<M> data(String data);

    static <M extends ExampleSave<M>> M create(ChannelPipeContext channelPipeContext) {
        return (M) new ExampleSaveMessage(channelPipeContext);
    }
}

class ExampleSaveMessage<M extends ExampleSaveMessage<M>>
        extends ChannelMessageImpl<M, String, Boolean>
        implements ExampleSave<M> {
    protected ExampleSaveMessage(ChannelPipeContext channelPipeContext) {
        super(channelPipeContext);
        this.messageType(ExampleMessageType.SAVE);
    }

    @Override
    public ExampleSave<M> data(String data) {
        return this.requestContent(data);
    }
}
