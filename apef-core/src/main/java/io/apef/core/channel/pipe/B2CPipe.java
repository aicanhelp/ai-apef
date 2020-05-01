package io.apef.core.channel.pipe;

import io.apef.core.channel.BusinessChannel;
import io.apef.core.channel.ClientChannel;
import io.apef.core.channel.request.B2CRequest;

public interface B2CPipe<S extends BusinessChannel<S>, D extends ClientChannel>
        extends ChannelTxPipe<S, D>, B2CNoReplyPipe<D> {
    B2CRequest<? extends B2CRequest, Object, Object> request();
}
