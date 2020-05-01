package io.apef.core.mock;

import io.apef.core.channel.MessageType;
import io.apef.core.channel.impl.ChannelMessageImpl;
import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;

@Test
public class ApecfMockStubberTest extends BaseUnitSpec {
    public void testMockResponse() {

        ApecfMockStubber apecfMockStubber = new ApecfMockStubber();

        MessageType messageType = MessageType.newType();

        ApecfMockStubber.ApecfMockStub<String, String> mockStub = apecfMockStubber.mock(messageType);

        mockStub.filter(s -> s.equals("a")).response("b")
                .endMock()
                .filter(s -> s.equals("b")).response("c")
                .endMock()
                .filter(s -> s.equals("c")).exception(new Exception("e"))
                .endMock();

        verifyRequest(apecfMockStubber, messageType, "a", "b");
        verifyRequest(apecfMockStubber, messageType, "b", "c");
        verifyException(apecfMockStubber, messageType, "c", "e");
    }

    private void verifyRequest(
            ApecfMockStubber apecfMockStubber,
            MessageType messageType,
            Object request, Object response) {
        Blocker blocker = new Blocker();
        TestChannelMessage<?, ?> channelMessage =
                new TestChannelMessage<>()
                        .onFailure((errMsg, cause) -> {
                            blocker.failAndEnd("should be success");
                        })
                        .onSuccess(outputValue -> {
                            blocker.assertEquals(outputValue, response).end();
                        })
                        .requestContent(request)
                        .messageType(messageType);
        channelMessage.end();
        apecfMockStubber.handle(channelMessage);
        blocker.awaitEnd().reset();
    }

    private void verifyException(
            ApecfMockStubber apecfMockStubber,
            MessageType messageType,
            Object request, Object response) {
        Blocker blocker = new Blocker();
        TestChannelMessage<?, ?> channelMessage =
                new TestChannelMessage<>()
                        .onFailure((errMsg, cause) -> {
                            blocker.assertEquals(cause.getMessage(), response).end();
                        })
                        .onSuccess(outputValue -> {
                            blocker.failAndEnd("should be failure");
                        })
                        .requestContent(request)
                        .messageType(messageType);
        channelMessage.end();
        apecfMockStubber.handle(channelMessage);
        blocker.awaitEnd().reset();
    }

    private static class TestChannelMessage<T, R> extends ChannelMessageImpl<TestChannelMessage<T, R>, T, R> {
        @Override
        protected void sendMessage() {

        }
    }
}