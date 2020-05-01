package io.apef.core.example.impl;

import io.apef.core.channel.ServerChannel;
import io.apef.core.channel.impl.ToBChannelPipeIml;
import io.apef.core.example.interfaces.ExampleChannel;
import io.apef.core.example.interfaces.ExampleChannelPipe;
import io.apef.core.example.interfaces.ExampleGet;
import io.apef.core.example.interfaces.ExampleSave;

public class ExampleChannelPipeImpl extends
        ToBChannelPipeIml implements ExampleChannelPipe {
    public ExampleChannelPipeImpl(ServerChannel srcChannel, ExampleChannel destChannel) {
        super(srcChannel, destChannel);
    }
    public ExampleGet get() {return ExampleGet.create(this);}
    public ExampleSave save() {return ExampleSave.create(this);}
}
