package io.apef.repository.message;

import io.apef.core.channel.future.ChannelFuture;
import io.apef.core.channel.impl.ChannelMessageImpl;
import io.apef.core.channel.pipe.ChannelPipeContext;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(fluent = true)
public abstract class RepositoryRequestMessage<M extends RepositoryRequestMessage<M, T, R>, T, R>
        extends ChannelMessageImpl<M, T, R>
        implements RepositoryRequest<M, T, R> {

    //for sync interface
    protected RepositoryRequestMessage(){}

    protected RepositoryRequestMessage(ChannelPipeContext channelPipeContext) {
        super(channelPipeContext);
    }

    protected void checkContentAndEnd(T content) {
        if (content != null) this.requestContent(content);
        if (this.requestContent() == null) {
            throw new IllegalArgumentException("RequestContent can not be null for Repository operation: " + this.messageType());
        }
        super.end();
    }

    protected ChannelFuture<R> checkContentAndFuture(T content) {
        if (content != null) this.requestContent(content);
        if (this.requestContent() == null) {
            throw new IllegalArgumentException("RequestContent can not be null for Repository operation: " + this.messageType());
        }
        return super.future();
    }
}
