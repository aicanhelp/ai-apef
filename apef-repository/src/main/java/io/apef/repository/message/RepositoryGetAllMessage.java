package io.apef.repository.message;

import io.apef.core.channel.pipe.ChannelPipeContext;
import io.apef.repository.RepositoryRequestType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.Set;


@Data
@Accessors(fluent = true)
public class RepositoryGetAllMessage<K, V>
        extends RepositoryRequestMessage<RepositoryGetAllMessage<K, V>,
        Set<K>, Map<K, V>>
        implements RepositoryGetAll<RepositoryGetAllMessage<K, V>, K, V> {
    private Set<K> keys;
    private Map<K, V> cachedValues;
    private boolean readThrough = true;

    public RepositoryGetAllMessage() {
        this.messageType(RepositoryRequestType.GET_ALL);
    }

    protected RepositoryGetAllMessage(ChannelPipeContext channelPipeContext) {
        super(channelPipeContext);
        this.messageType(RepositoryRequestType.GET_ALL);
    }

    @Override
    public void end() {
        super.checkContentAndEnd(this.keys);
    }
}
