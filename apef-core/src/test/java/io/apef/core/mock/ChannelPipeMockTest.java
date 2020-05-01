package io.apef.core.mock;

import io.apef.core.APEF;
import io.apef.core.channel.ChannelConfig;
import io.apef.core.channel.MessageType;
import io.apef.core.channel.impl.FromBChannelPipeIml;
import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;

@Test
public class ChannelPipeMockTest extends BaseUnitSpec {

    public void testMockResponse() {
        ChannelPipeMock<TestChannelPipe> channelPipeMock = ChannelPipeMock.mock();
        TestChannelPipe mockChannelPipe=channelPipeMock.bind(new TestChannelPipe());
        MessageType messageType1 = MessageType.newType(0);
        MessageType messageType2 = MessageType.newType(1);

        ApecfMockStubber.ApecfMockStub<String, String>
                requestMock1 = channelPipeMock.mock(messageType1);

        ApecfMockStubber.ApecfMockStub<Integer, Integer>
                requestMock2 = channelPipeMock.mock(messageType2);

        requestMock1.filter(s -> s.equals("a"))
                .response("b").endMock()
                .filter(s -> s.equals("b")).response("c").endMock();

        requestMock2.filter(integer -> integer == 0).response(1).endMock()
                .filter(integer -> integer == 1).response(2).endMock();

        verifyRequest(mockChannelPipe, messageType1, "a", "b");
        verifyRequest(mockChannelPipe, messageType1, "b", "c");
        verifyRequest(mockChannelPipe, messageType2, 0, 1);
        verifyRequest(mockChannelPipe, messageType2, 1, 2);
    }

    private void verifyRequest(TestChannelPipe channelPipe,
                               MessageType messageType,
                               Object request, Object response) {
        Blocker blocker = new Blocker();
        channelPipe.request()
                .requestContent(request)
                .messageType(messageType)
                .onFailure((errMsg, cause) -> {
                    blocker.failAndEnd("should be success");
                })
                .onSuccess(outputValue -> {
                    blocker.assertEquals(outputValue, response).end();
                })
                .end();
        blocker.awaitEnd().reset();
    }

    public static class TestChannelPipe extends FromBChannelPipeIml {

        public TestChannelPipe() {
            super(APEF.createBusinessChannel(new ChannelConfig().setName("b")),
                    APEF.createBusinessChannel(new ChannelConfig().setName("c")));
        }
    }
}