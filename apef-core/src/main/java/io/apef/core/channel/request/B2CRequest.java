package io.apef.core.channel.request;


public interface B2CRequest<M extends B2CRequest<M, T, R>, T, R>
        extends ChannelTxRequest<M, T, R>, B2CNoReplyRequest<M, T> {
    /**
     * retry
     *
     * @param retryCount
     * @param intervalMs
     * @return
     */
    M retry(int retryCount, int intervalMs);
}