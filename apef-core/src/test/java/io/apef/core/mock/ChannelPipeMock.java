package io.apef.core.mock;

import io.apef.core.channel.MessageType;
import io.apef.core.channel.impl.ChannelMessageImpl;
import io.apef.core.channel.impl.FromBChannelPipeSender;
import io.apef.core.channel.impl.ToBChannelPipeAcceptor;
import io.apef.core.channel.pipe.ChannelInternalPipe;
import io.apef.core.channel.pipe.ChannelPipe;
import io.apef.core.channel.pipe.ChannelPipeSender;
import lombok.Getter;
import lombok.experimental.Accessors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ChannelPipeMock<C extends ChannelPipe> {

    @Getter
    @Accessors(fluent = true)
    private C mockChannelPipe;

    private boolean byInterceptor;

    private ApecfMockStubber mockStubber = new ApecfMockStubber();

    private ChannelPipeMock(boolean byInterceptor) {
        this.byInterceptor = byInterceptor;
    }

    private ChannelPipeMock() {
        this.byInterceptor = true;
    }

    public C bind(C channelPipe) {
        if (this.mockChannelPipe != null) return this.mockChannelPipe;
        if (byInterceptor)
            this.mockChannelPipe = this.mockChannelPipeByInterceptor(channelPipe);
        else
            this.mockChannelPipe = this.mockChannelPipe(channelPipe);
        return this.mockChannelPipe;
    }

    /**
     * Poor performance mock using mockito
     *
     * @param <C>
     * @return
     */
    @Deprecated
    public static <C extends ChannelPipe> ChannelPipeMock<C> mock2() {
        return new ChannelPipeMock<C>(false);
    }

    /**
     * High performance mock which is not use mockito
     *
     * @param <C>
     * @return
     */
    public static <C extends ChannelPipe> ChannelPipeMock<C> mock() {
        return new ChannelPipeMock<C>(true);
    }

    private C mockChannelPipeByInterceptor(C channelPipe) {
        ChannelInternalPipe channelInternalPipe = (ChannelInternalPipe) channelPipe;
        if (channelInternalPipe.acceptor() instanceof ToBChannelPipeAcceptor) {
            ((ToBChannelPipeAcceptor) channelInternalPipe.acceptor())
                    .setInterceptor(new MockToBInterceptor(this.mockStubber));
        }

        if (channelInternalPipe.sender() instanceof FromBChannelPipeSender) {
            ((FromBChannelPipeSender) channelInternalPipe.sender()).setInterceptor(new MockFromBInterceptor(this.mockStubber));
        }

        return channelPipe;
    }

    private C mockChannelPipe(C channelPipe) {
        ChannelInternalPipe channelInternalPipe = spy((ChannelInternalPipe) channelPipe);

        ChannelPipeSender mockSender = spy(channelInternalPipe.sender());

        when(channelInternalPipe.sender()).thenReturn(mockSender);

        doAnswer(invocation -> {
            ChannelMessageImpl channelMessage = invocation.getArgument(0);

            mockStubber.handle(channelMessage);
            return null;
        }).when(mockSender).send(any());

        return (C) channelInternalPipe;
    }

    public synchronized <T, R> ApecfMockStubber.ApecfMockStub<T, R> mock(MessageType messageType,
                                                                         Class<T> requestType, Class<R> responseType) {
        return mockStubber.mock(messageType);
    }

    public synchronized <T, R> ApecfMockStubber.ApecfMockStub<T, R> mock(MessageType messageType) {
        return mockStubber.mock(messageType);
    }
}
