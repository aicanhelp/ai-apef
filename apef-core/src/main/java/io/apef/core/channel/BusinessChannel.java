package io.apef.core.channel;

import io.apef.core.channel.pipe.*;

public interface BusinessChannel<C extends BusinessChannel<C>> extends Channel<C> {
    B2BNoReplyPipe<C> B2BNoReplyPipe();

    B2BPipe<?, C> B2BPipe(BusinessChannel fromChannel);

    S2BNoReplyPipe<C> S2BNoReplyPipe();

    S2BPipe<?, C> S2BPipe(ServerChannel fromChannel);
}
