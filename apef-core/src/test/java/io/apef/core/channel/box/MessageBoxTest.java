package io.apef.core.channel.box;

import io.apef.core.channel.TestMessageType;
import io.apef.testing.unit.BaseUnitSpec;

public abstract class MessageBoxTest extends BaseUnitSpec {
    protected void doTest(MessageBox messageBox) {
        Blocker type1Blocker = new Blocker();
        Blocker type2Blocker = new Blocker();
        messageBox.<Integer>handler(TestMessageType.Type1, (messageType, message) -> {
            type1Blocker.assertTrue(message < 20000).endIf(message > 10000);
        }).<Integer>handler(TestMessageType.Type2, (messageType, message) -> {
            type2Blocker.assertTrue(message > 20000).endIf(message > 30000);
        });
        messageBox.start();

        for (int i = 0; i < 10100; i++) {
            messageBox.put(TestMessageType.Type1, i);
        }

        for (int i = 20001; i < 30100; i++) {
            messageBox.put(TestMessageType.Type2, i);
        }

        type1Blocker.awaitEnd();
        type2Blocker.awaitEnd();
        messageBox.close();
    }
}
