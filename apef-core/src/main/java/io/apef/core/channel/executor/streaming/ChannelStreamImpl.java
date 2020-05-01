package io.apef.core.channel.executor.streaming;

import io.apef.core.channel.future.ChannelFuture;
import io.apef.core.channel.future.ChannelFutureImpl;


public class ChannelStreamImpl<O> extends ChannelFutureImpl<O> implements ChannelStream<O> {

    private ChannelFuture<O> future;

    public ChannelStreamImpl(ChannelFuture<O> future) {
        this.future = future;
    }

    ChannelStreamImpl() {

    }

    @Override
    public <O2> ChannelStream<O2> map(ChannelStream.SuccessMapper<O, O2> mapper) {
        ChannelStreamImpl<O2> next = new ChannelStreamImpl<>();
        next.future = next;

        this.future.onSuccess(outputValue -> {
            mapper.map(outputValue)
                    .onSuccess(next::complete)
                    .onFailure(next::complete);
        });

        return next;
    }

    @Override
    public <O2> ChannelStream<O2> map(SuccessMapper<O, O2> successMapper, FailureMapper<O2> failureMapper) {
        ChannelStreamImpl<O2> next = new ChannelStreamImpl<>();
        next.future = next;

        if (successMapper != null)
            this.future.onSuccess(outputValue -> {
                ChannelFuture<O2> future = successMapper.map(outputValue);
                if (future == null) {
                    next.complete(null);
                } else
                    future.onSuccess(next::complete)
                            .onFailure(next::complete);
            });

        if (failureMapper != null) {
            this.future.onFailure((errMsg, cause) -> {
                ChannelFuture<O2> future = failureMapper.map(errMsg, cause);

                if (future == null) {
                    next.complete(null);
                } else
                    future.onSuccess(next::complete)
                            .onFailure(next::complete);
            });
        }

        return next;
    }


    @Override
    public ChannelFuture<O> future() {
        return this.future;
    }
}
