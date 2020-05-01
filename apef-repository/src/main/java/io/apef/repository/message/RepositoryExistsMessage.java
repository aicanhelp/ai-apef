package io.apef.repository.message;

import io.apef.core.channel.pipe.ChannelPipeContext;
import io.apef.repository.RepositoryRequestType;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(fluent = true)
public class RepositoryExistsMessage<K, V>
        extends RepositoryRequestMessage<RepositoryExistsMessage<K, V>, K, Boolean>
        implements RepositoryExists<RepositoryExistsMessage<K, V>, K> {
    private K key;
    private boolean hitCache;
    private V value;
    private boolean readThrough=true;

    public RepositoryExistsMessage(){
        this.messageType(RepositoryRequestType.EXISTS);
    }

    public RepositoryExistsMessage(ChannelPipeContext channelPipeContext) {
        super(channelPipeContext);
        this.messageType(RepositoryRequestType.EXISTS);
    }

    @Override
    public void end() {
        super.checkContentAndEnd(this.key);
    }
}
