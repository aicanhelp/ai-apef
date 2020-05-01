package io.apef.connector.sender;

import io.apef.core.channel.MessageType;
import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;

@Test
public class MockSenderTest extends BaseUnitSpec {
    public void testMockSender() {
        MockSender<String, String, String> mockSender =
                new MockSender<>(String::toString);

        mockSender.mock("a", "b")
                .mock("b", new Exception("c"));

        Blocker blocker1 = new Blocker();

        mockSender.mockRequest()
                .requestType(MessageType.newType())
                .requestContent("a")
                .onSuccess(outputValue -> {
                    blocker1.assertEquals(outputValue, "b").end();
                })
                .onFailure((errMsg, cause) -> {
                    blocker1.failAndEnd("should be success,errMsg: " + errMsg);
                })
                .end();

        blocker1.awaitEnd().reset();

        mockSender.mockRequest()
                .requestType(MessageType.newType())
                .requestContent("b")
                .onSuccess(outputValue -> {
                    blocker1.failAndEnd("should be failure");
                })
                .onFailure((errMsg, cause) -> {
                    blocker1.assertEquals(cause.getMessage(), "c").end();
                })
                .end();

        blocker1.awaitEnd();
    }
}