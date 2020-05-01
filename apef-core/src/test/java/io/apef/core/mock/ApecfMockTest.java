package io.apef.core.mock;

import io.apef.core.channel.impl.ChannelMessageImpl;
import io.apef.core.channel.impl.FromBChannelPipeIml;
import io.apef.core.channel.request.ChannelTxRequest;
import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;

@Test
public class ApecfMockTest extends BaseUnitSpec {
    ApecfMock<String, String, String> mockManager = new ApecfMock<>(Object::toString);

    public void mockResponseCorrect() {
        MockTestChannelPipe channelPipe = new MockTestChannelPipe();

        mockManager
                .mock("a")
                .response("b")
                .endMock();

        Blocker blocker = new Blocker();
        channelPipe.get()
                .onSuccess(outputValue -> {
                    blocker.assertEquals(outputValue, "b").end();
                })
                .onFailure((errMsg, cause) -> {
                    blocker.failAndEnd("should be success");
                })
                .requestContent("a")
                .end();
        blocker.awaitEnd();
    }

    public void mockResponseException() {
        MockTestChannelPipe channelPipe = new MockTestChannelPipe();

        mockManager
                .mock("b")
                .exception(new Exception("e"))
                .endMock();

        Blocker blocker = new Blocker();
        channelPipe.get()
                .onSuccess(outputValue -> {
                    blocker.failAndEnd("should be failure");
                })
                .onFailure((errMsg, cause) -> {
                    blocker.assertEquals(cause.getMessage(), "e").end();
                })
                .requestContent("b")
                .end();
        blocker.awaitEnd();
    }

    public void testFilter() {
        MockTestChannelPipe channelPipe = new MockTestChannelPipe();

        mockManager
                .mock()
                .filter(key -> key.contains("a"))
                .response("b")
                .endMock();

        for (int i = 0; i < 5; i++) {
            Blocker blocker = new Blocker();
            channelPipe.get()
                    .onSuccess(outputValue -> {
                        blocker.assertEquals(outputValue, "b").end();
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker.failAndEnd("should be success");
                    })
                    .requestContent("a_" + i)
                    .end();
            blocker.awaitEnd();
        }
    }

    public void testPooledMock() {
        MockTestChannelPipe channelPipe = new MockTestChannelPipe();

        ApecfRequestMockTest.TestRequest[] gets = new ApecfRequestMockTest.TestRequest[10];

        for (int i = 0; i < 10; i++) {
            gets[i] = channelPipe.get();
            mockManager.mock("a" + i)
                    .response("b" + i)
                    .endMock();
        }

        for (int i = 0; i < 10; i++) {
            Blocker blocker = new Blocker();
            String request = "a" + i;
            String response = "b" + i;
            gets[i].onSuccess(outputValue -> {
                blocker.assertEquals(outputValue, response).end();
            }).onFailure((errMsg, cause) -> {
                blocker.failAndEnd("should be success");
            }).requestContent(request)
                    .end();
            blocker.awaitEnd();
        }
    }

    public class MockTestChannelPipe extends FromBChannelPipeIml {
        public MockTestChannelPipe() {
            super(null, null);
        }

        public ApecfRequestMockTest.TestRequest get() {
            return mockManager.mockRequest(new TestRequestMessage());
        }
    }

    public interface TestRequest extends ChannelTxRequest<TestRequest, String, String> {

    }

    public class TestRequestMessage extends ChannelMessageImpl
            implements ApecfRequestMockTest.TestRequest {

    }
}