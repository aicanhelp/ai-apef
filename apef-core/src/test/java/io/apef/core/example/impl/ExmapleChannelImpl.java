package io.apef.core.example.impl;

import io.apef.core.channel.BusinessChannelImpl;
import io.apef.core.channel.ChannelConfig;
import io.apef.core.channel.message.ChannelMessageContext;
import io.apef.core.example.interfaces.ExampleChannel;
import io.apef.core.example.interfaces.ExampleMessageType;

public class ExmapleChannelImpl extends BusinessChannelImpl<ExmapleChannelImpl>
        implements ExampleChannel<ExmapleChannelImpl> {
    public ExmapleChannelImpl(ChannelConfig channelConfig) {
        super(channelConfig);
        this.handler(ExampleMessageType.GET, this::handleGet)
                .handler(ExampleMessageType.SAVE, this::handleSave)
                .start();
    }

    void handleGet(ChannelMessageContext<String> messageContext, Integer requestContent) {
        messageContext.succeed("OK");
    }

    void handleSave(ChannelMessageContext<String> messageContext, Integer requestContent) {
        messageContext.succeed("OK");
    }
}
