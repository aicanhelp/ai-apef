package io.apef.core.mock;

import io.apef.core.channel.impl.ChannelMessageImpl;
import io.apef.core.channel.impl.ToBInterceptorImpl;
import io.apef.core.channel.message.ChannelInternalRequestMessage;

public class MockToBInterceptor extends ToBInterceptorImpl {
    private ApecfMockStubber mockStubber;

    public MockToBInterceptor(ApecfMockStubber mockStubber) {
        this.mockStubber = mockStubber;
        mockStubber.onMock(messageType -> {
            MockToBInterceptor.this.beforeHandleRequest(messageType, MockToBInterceptor.this::handleRequest);
        });
    }

    private void handleRequest(ChannelInternalRequestMessage requestMessage) {
        mockStubber.handle((ChannelMessageImpl) requestMessage);
    }
}
