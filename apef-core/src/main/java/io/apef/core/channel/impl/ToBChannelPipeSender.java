package io.apef.core.channel.impl;

import io.apef.core.channel.BusinessChannel;
import io.apef.core.channel.message.ChannelInternalRequestMessage;
import io.apef.core.channel.pipe.ChannelPipeSender;
import lombok.AllArgsConstructor;

import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class ToBChannelPipeSender implements ChannelPipeSender {
    private BusinessChannel<?> destChannel;

    @Override
    public <T, R> void send(ChannelInternalRequestMessage<T, R> message) {
        //for To Business ChannelPipe, because idem is controlled by business Channel
        //So whatever, the message must be sent
        this.destChannel.write(message);
    }

    @Override
    public <T, R> void retry(ChannelInternalRequestMessage<T, R> message, int delayMs) {
        message.channelPipe()
                .srcChannel()
                .schedule(message::retry,
                        delayMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public <T, R> void handleResponse(ChannelInternalRequestMessage<T, R> message) {

        if (message.features().idem() != null) {
            message.features().idem().finishFollowers();
        }

        message.finish();
    }
}
