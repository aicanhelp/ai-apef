package io.apef.repository.message;

import io.apef.core.channel.pipe.ChannelPipeContext;
import io.apef.repository.RepositoryRequestType;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class RepositoryGetMessage<K, V>
        extends RepositoryRequestMessage<RepositoryGetMessage<K, V>, K, V>
        implements RepositoryGet<RepositoryGetMessage<K, V>, K, V> {
    private K key;
    private boolean hitCache;
    private boolean readThrough = true;

    public RepositoryGetMessage() {
        this.messageType(RepositoryRequestType.GET);
    }

    public RepositoryGetMessage(ChannelPipeContext channelPipeContext) {
        super(channelPipeContext);
        this.messageType(RepositoryRequestType.GET);
    }

    @Override
    public void end() {
        super.checkContentAndEnd(this.key);
    }
}
