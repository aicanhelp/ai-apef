package io.apef.repository;

import io.apef.core.channel.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public enum RepositoryRequestType implements MessageType<RepositoryRequestType> {
    GET((byte) 0),
    SAVE((byte) 1),
    GET_ALL((byte) 2),
    PUT_ALL((byte) 3),
    EXISTS((byte) 4);

    private byte id;
}
