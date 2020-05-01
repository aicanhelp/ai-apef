package io.apef.core.channel.request;

import io.apef.core.channel.MessageType;
import io.apef.metrics.item.MetricItemTimer;

public interface ChannelRequest<M extends ChannelRequest<M, T>, T> {
    /**
     * Set this message as Idempotent with key
     *
     * @param key
     * @return
     */
    M idem(Object key);

    /**
     * Set message type, it is required.
     *
     * @param messageType
     * @return
     */
    M messageType(MessageType messageType);

    M metric(MetricItemTimer metricItemTimer);

    /**
     * RequestContent
     *
     * @param requestContent
     * @return
     */
    M requestContent(T requestContent);

    /**
     * End and send this request message
     */
    void end();
}
