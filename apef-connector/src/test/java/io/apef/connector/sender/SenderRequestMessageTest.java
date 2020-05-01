package io.apef.connector.sender;

import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;

@Test
public class SenderRequestMessageTest extends BaseUnitSpec {
    public void testMockRequest() {
        Blocker blocker = new Blocker();
        SenderRequestMessage.mockRequest(() -> 1000)
                .onSuccess(outputValue -> {
                    blocker.assertEquals(outputValue, 1000).end();
                }).end();
        blocker.awaitEnd();
    }
}