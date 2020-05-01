package io.apef.core.example.impl;

import io.apef.core.channel.ChannelConfig;
import io.apef.core.channel.ServerChannel;
import io.apef.core.example.interfaces.ExampleChannel;
import io.apef.core.example.interfaces.ExampleChannelPipe;
import io.apef.core.example.interfaces.IExampleService;

public class ExampleService implements IExampleService {
    private ExampleChannel exampleChannel = new ExmapleChannelImpl(new ChannelConfig().setName(""));

    @Override
    public ExampleChannelPipe channelPipe(ServerChannel srcChannel) {
        return new ExampleChannelPipeImpl(srcChannel, exampleChannel);
    }
}
