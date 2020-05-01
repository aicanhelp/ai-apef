package io.apef.core.example.interfaces;

import io.apef.core.channel.BusinessChannel;
import io.apef.core.channel.ServerChannel;
import io.apef.core.channel.pipe.S2BPipe;

public interface ExampleChannelPipe<S extends ServerChannel,
        D extends BusinessChannel<D>> extends S2BPipe<S, D> {
    <M extends ExampleGet<M>> M get();

    <M extends ExampleSave<M>> M save();
}
