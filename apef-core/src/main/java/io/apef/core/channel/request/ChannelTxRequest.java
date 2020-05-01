package io.apef.core.channel.request;

import io.apef.core.channel.future.FailureHandler;
import io.apef.core.channel.future.SuccessHandler;
import io.apef.core.channel.future.ChannelFuture;

public interface ChannelTxRequest<M extends ChannelTxRequest<M, T, R>, T, R>
        extends ChannelRequest<M, T> {
    /**
     * Set timeout for this request
     *
     * @param timeoutMS
     * @return
     */
    M timeout(int timeoutMS);

    /**
     * Set the failure response handler
     *
     * @param failureHandler
     * @return
     */
    M onFailure(FailureHandler failureHandler);

    /**
     * Set the success response handler
     *
     * @param successHandler
     * @return
     */
    M onSuccess(SuccessHandler<R> successHandler);

    /**
     * return a future object for getting result in the future
     *
     * @return
     */
    ChannelFuture<R> future();

}
