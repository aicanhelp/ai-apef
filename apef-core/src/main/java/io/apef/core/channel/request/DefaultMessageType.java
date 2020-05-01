package io.apef.core.channel.request;

import io.apef.core.channel.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public enum DefaultMessageType implements MessageType<DefaultMessageType> {
    Response((byte) 25), Timeout((byte) 26), Schedule((byte) 27);

    private byte id;
}
