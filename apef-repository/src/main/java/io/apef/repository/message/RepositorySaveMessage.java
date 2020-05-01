package io.apef.repository.message;

import io.apef.core.channel.future.ChannelFuture;
import io.apef.core.channel.pipe.ChannelPipeContext;
import io.apef.repository.RepositoryRequestType;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(fluent = true)
public class RepositorySaveMessage<V>
        extends RepositoryRequestMessage<RepositorySaveMessage<V>,
        V, Boolean>
        implements RepositorySave<RepositorySaveMessage<V>, V> {
    private V value;

    public RepositorySaveMessage() {
        this.messageType(RepositoryRequestType.SAVE);
    }

    protected RepositorySaveMessage(ChannelPipeContext channelPipeContext) {
        super(channelPipeContext);
        this.messageType(RepositoryRequestType.SAVE);
    }

    @Override
    public void end() {
        super.checkContentAndEnd(this.value);
    }

    @Override
    public ChannelFuture<Boolean> future() {
        return super.checkContentAndFuture(this.value);
    }
}
