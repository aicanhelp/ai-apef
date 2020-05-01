package io.apef.core.example.interfaces;


import io.apef.core.channel.ServerChannel;

public interface IExampleService {
    ExampleChannelPipe channelPipe(ServerChannel srcChannel);
}
