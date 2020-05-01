package io.apef.core.channel.executor.sequential;

import io.apef.core.channel.future.ChannelFuture;
import io.apef.core.channel.future.ChannelFutureImpl;

public interface SequentialFuture<T> extends ChannelFuture<T> {

}

class SequentialFutureImpl<T> extends ChannelFutureImpl<T>
        implements SequentialFuture<T> {

}
