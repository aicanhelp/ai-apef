package io.apef.core.channel.executor.batch;


import io.apef.core.channel.future.ChannelFuture;
import io.apef.core.channel.future.ChannelFutureImpl;

public interface BatchFuture<T> extends ChannelFuture<T> {

}

class BatchFutureImpl<T> extends ChannelFutureImpl<T>
        implements BatchFuture<T> {

}
