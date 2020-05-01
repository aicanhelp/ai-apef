package io.apef.core.channel.future;

public interface ChannelCompletableFuture<O> extends ChannelFuture<O> {
    void complete(O value);

    void complete(String errMsg, Throwable cause);
}
