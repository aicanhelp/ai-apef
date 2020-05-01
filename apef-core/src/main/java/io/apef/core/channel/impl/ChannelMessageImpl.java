package io.apef.core.channel.impl;

import com.codahale.metrics.Timer;
import io.apef.core.channel.future.FailureHandler;
import io.apef.core.channel.future.SuccessHandler;
import io.apef.core.channel.feature.timeout.TimeoutException;
import io.apef.core.channel.future.ChannelFuture;
import io.apef.core.channel.future.ChannelFutureImpl;
import io.apef.core.channel.message.ChannelInternalRequestMessage;
import io.apef.core.channel.message.MessageStatus;
import io.apef.core.channel.pipe.ChannelPipeContext;
import io.apef.metrics.item.MetricItemTimer;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public class ChannelMessageImpl<M extends ChannelMessageImpl<M, T, R>, T, R>
        extends AbstractChannelMessageImpl<M, T, R> {

    @Getter
    @Accessors
    private boolean noReply = false;
    @Getter
    @Accessors(fluent = true)
    private MessageStatus status;
    private MetricItemTimer metricItemTimer;
    private Timer.Context metricContext;

    @Getter
    @Accessors(fluent = true)
    private ChannelInternalResponse<T, R> response;

    @Getter
    @Accessors(fluent = true)
    private ChannelFutureImpl<R> responseFuture = new ChannelFutureImpl<>();

    protected ChannelMessageImpl() {
    }

    public ChannelMessageImpl(ChannelPipeContext channelPipeContext) {
        super(channelPipeContext);
        if (channelPipeContext.channelPipe() != null)
            this.noReply = !channelPipeContext.channelPipe().hasReply();
    }

    @Override
    public M metric(MetricItemTimer metricItemTimer) {
        this.metricItemTimer = metricItemTimer;
        return (M) this;
    }

    /**
     * send message only
     */
    @Override
    public void end() {
        //Don't use Preconditions.checkNotNull, low performance
        if (this.messageType() == null) {
            throw new NullPointerException("MessageType is required for " + this.toString());
        }

        if (!noReply) {
            if ((!this.responseFuture.notNullHandler()))
                throw new NullPointerException("SuccessHandler and FailureHandler is required for " + this.toString());
            this.response = new ChannelInternalResponse<>();
        }

        this.sendMessage();
    }

    @Override
    public void retry() {
        this.sendMessage();
    }

    protected void sendMessage() {
        this.status = MessageStatus.Sent;
        if (this.metricItemTimer != null) {
            this.metricContext = this.metricItemTimer.start();
        }
        this.channelPipe().sender().send(this);
    }

    /**
     * Send message and return future
     *
     * @return
     */
    public ChannelFuture<R> future() {
        if (noReply) {
            throw new IllegalArgumentException("Future is not supported for NoReply request");
        }
        if (this.messageType() == null) {
            throw new NullPointerException("MessageType is required for " + this.toString());
        }

        if (!noReply) {
            this.response = new ChannelInternalResponse<>();
        }

        this.sendMessage();
        return this.responseFuture;
    }

    @Override
    public M onFailure(FailureHandler failureHandler) {
        this.responseFuture.onFailure(failureHandler);
        return (M) this;
    }

    @Override
    public M onSuccess(SuccessHandler<R> successHandler) {
        this.responseFuture.onSuccess(successHandler);
        return (M) this;
    }

    /**
     * This method is called by timer thread,
     * send the timeout event.
     */
    @Override
    public void timeout() {
        this.channelPipe().timeout(this);
    }

    /**
     * handle timeout event
     */
    public void finishByTimeout() {
        if (this.status == MessageStatus.Finished) return;
        this.status = MessageStatus.Timeout;
        if (!this.noReply) {
            this.response()
                    .success(false)
                    .cause(TimeoutException.INSTANCE)
                    .errMsg(TimeoutException.INSTANCE.getMessage());
        }
        this.finish();
    }

    @Override
    public void finishByFollow(ChannelInternalRequestMessage<T, R> message) {
        this.status = message.status();
        this.response = message.response();
        this.finish();
    }

    /**
     * Send failure event
     * (1) For S2B ChannelPipe, it's invoked in Business ThreadContext
     * (2) For B2C ChannelPipe, it's invoked in Client ThreadContext
     *
     * @param errMsg
     * @param ex
     */
    @Override
    public void fail(String errMsg, Throwable ex) {
        if (!this.noReply) {
            this.response()
                    .success(false)
                    .cause(ex).errMsg(errMsg);
            if (this.channelPipe() != null)
                this.channelPipe().acceptor().response(this);
        }
    }

    /**
     * Send success event
     * (1) For S2B ChannelPipe, it's invoked in Business ThreadContext
     * (2) For B2C ChannelPipe, it's invoked in Client ThreadContext
     */
    @Override
    public void succeed(R responseContent) {
        if (!this.noReply) {
            this.response()
                    .success(true)
                    .responseContent(responseContent);
            if (this.channelPipe() != null)
                this.channelPipe().acceptor().response(this);
        }
    }

    /**
     * finish this message with current result
     */
    //finish() always triggered by the business threadContext
    @Override
    public void finish() {
        if (this.status == MessageStatus.Finished) {
            if (this.metricContext != null) this.metricContext.stop();
            return;
        }

        this.features().cancelTimeout();
        //Special Cases:
        //1. timeout and succeed() are triggered at the same time:
        //   (1) finish() is called by timeout, but the response is set by success, then complete with success
        //       because succeed() maybe trigger in other thread
        //   (2) finish() is triggered by succeed, timeout will can not triggered the finish()
        //       because finish() always triggered by the business threadContext
        //2. 1. timeout and succeed() are triggered at the same time on retry:
        //   (1) finish() is called by timeout and complete with timeout, and then the response set by success,
        //       at the same time, the followed messages are retried.
        //       Then the followed messages should completed with this success response.
        if (!this.noReply) {
            if (!this.response.success())
                this.responseFuture.complete(this.response.errMsg, this.response.cause);
            else
                this.responseFuture.complete(this.response.responseContent);
        }
        this.status = MessageStatus.Finished;
        if (this.metricContext != null) this.metricContext.stop();

    }

    @Override
    public M noReply() {
        this.noReply = true;
        return (M) this;
    }

    /**
     * For mock test only
     *
     * @param responseSupplicer
     */
    protected void end(Supplier<R> responseSupplicer) {
        try {
            responseFuture().complete(responseSupplicer.get());
        } catch (Exception ex) {
            responseFuture().complete(ex.getMessage(), ex);
        }
    }

}
