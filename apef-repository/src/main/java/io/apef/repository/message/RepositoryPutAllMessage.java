package io.apef.repository.message;

import io.apef.core.channel.pipe.ChannelPipeContext;
import io.apef.repository.RepositoryRequestType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;


@Data
@Accessors(fluent = true)
public class RepositoryPutAllMessage<K, V>
        extends RepositoryRequestMessage<RepositoryPutAllMessage<K, V>,
        Map<K, V>, Boolean>
        implements RepositoryPutAll<RepositoryPutAllMessage<K, V>, K, V> {
    private Map<K, V> values;

    public RepositoryPutAllMessage() {
        this.messageType(RepositoryRequestType.PUT_ALL);
    }

    protected RepositoryPutAllMessage(ChannelPipeContext channelPipeContext) {
        super(channelPipeContext);
        this.messageType(RepositoryRequestType.PUT_ALL);
    }

    @Override
    public void end() {
        super.checkContentAndEnd(this.values);
    }
}
