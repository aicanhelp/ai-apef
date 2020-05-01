package io.apef.core.example.interfaces;

import io.apef.core.channel.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public enum ExampleMessageType implements MessageType<ExampleMessageType> {
    GET((byte) 0), SAVE((byte) 1);

    private byte id;
}
