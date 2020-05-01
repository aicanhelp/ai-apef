package io.apef.core.channel.box;

import io.netty.util.concurrent.DefaultThreadFactory;
import org.testng.annotations.Test;

public class DisruptorMessageBoxTest extends MessageBoxTest {

    @Test
    public void testMessageHandler() {
        DisruptorMessageBox messageBox =
                new DisruptorMessageBox("testBox", 1024, 1, new DefaultThreadFactory("Test"));
        doTest(messageBox);
    }
}