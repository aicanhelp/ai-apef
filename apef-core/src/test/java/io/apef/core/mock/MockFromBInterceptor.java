package io.apef.core.mock;

import io.apef.core.channel.impl.ChannelMessageImpl;
import io.apef.core.channel.impl.FromBInterceptorImpl;
import io.apef.core.channel.message.ChannelInternalRequestMessage;

public class MockFromBInterceptor extends FromBInterceptorImpl {
    private ApecfMockStubber mockStubber;

    public MockFromBInterceptor(ApecfMockStubber mockStubber) {
        this.mockStubber = mockStubber;
        mockStubber.onMock(messageType -> {
            MockFromBInterceptor.this.beforeSendRequest(messageType, MockFromBInterceptor.this::handleRequest);
        });
    }

    private void handleRequest(ChannelInternalRequestMessage requestMessage) {
        mockStubber.handle((ChannelMessageImpl) requestMessage);
    }
}
