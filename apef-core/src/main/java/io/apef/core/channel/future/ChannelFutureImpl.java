package io.apef.core.channel.future;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class ChannelFutureImpl<O> implements ChannelFuture<O>,
        ChannelCompletableFuture<O> {

    private FutureStatus status = FutureStatus.NOT_COMPLETED;
    private String errMsg;
    private Throwable cause;
    private O value;

    private SuccessHandlerChain<O> successHandlerChain;
    private FailureHandlerChain failureHandlerChain;

    @Override
    public void complete(O value) {
        this.status = FutureStatus.SUCCESS;
        this.value = value;
        if (this.successHandlerChain != null)
            this.successHandlerChain.complete(value);
    }

    public void reset() {
        this.status = FutureStatus.NOT_COMPLETED;
        this.cause = null;
        this.errMsg = null;
        this.value = null;
        this.successHandlerChain = null;
        this.failureHandlerChain = null;
    }

    public boolean isCompleted() {
        return this.status != FutureStatus.NOT_COMPLETED;
    }

    protected void complete(boolean success) {
        if (success)
            this.complete(null);
        else
            this.complete(null, null);
    }

    public void follow(ChannelFuture<O> channelFuture) {
        channelFuture.onSuccess(this::complete)
                .onFailure(this::complete);
    }

    @Override
    public void complete(String errMsg, Throwable cause) {
        this.status = FutureStatus.FAILURE;
        this.errMsg = errMsg;
        this.cause = cause;
        if (this.failureHandlerChain != null)
            this.failureHandlerChain.complete(this.errMsg, this.cause);
    }


    @Override
    public ChannelFuture<O> onSuccess(SuccessHandler<O> successHandler) {
        if (successHandler == null) return this;
        if (this.status == FutureStatus.SUCCESS) {
            successHandler.handle(this.value);
            return this;
        }
        if (this.status == FutureStatus.NOT_COMPLETED) {
            if (this.successHandlerChain == null) {
                this.successHandlerChain = new SuccessHandlerChain<O>(successHandler);
            } else {
                this.successHandlerChain = this.successHandlerChain.next(successHandler);
            }
        }
        return this;
    }

    @Override
    public ChannelFuture<O> onFailure(FailureHandler failureHandler) {
        if (failureHandler == null) return this;
        if (this.status == FutureStatus.FAILURE) {
            failureHandler.handle(this.errMsg, this.cause);
            return this;
        }
        if (this.status == FutureStatus.NOT_COMPLETED) {
            if (this.failureHandlerChain == null) {
                this.failureHandlerChain = new FailureHandlerChain(failureHandler);
            } else {
                this.failureHandlerChain = this.failureHandlerChain.next(failureHandler);
            }
        }
        return this;
    }

    public boolean notNullHandler() {
        return this.failureHandlerChain != null && this.successHandlerChain != null;
    }

    enum FutureStatus {
        NOT_COMPLETED, SUCCESS, FAILURE;
    }
}
