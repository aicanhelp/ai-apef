package io.apef.core.channel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public enum TestMessageType implements MessageType<TestMessageType> {
    Type1((byte) 0),
    Type2((byte) 1),
    Type3((byte) 2),
    Type4((byte) 3);

    private byte id;
}
