package io.apef.core.channel;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Arrays;

public interface MessageType<T extends MessageType<T>> extends Comparable<T> {
    MessageType BY_PASS = new DefaultMessageType((byte) -2);
    MessageType NO_TYPE = new DefaultMessageType((byte) -1);
    byte MAX_ID = 31;
    byte MAX_USER_ID = 23;

    /**
     * Custom id Value is from 0~23,
     * 24~31 is for other
     *
     * @return
     */
    byte id();

    default boolean isNoType() {
        return id() == NO_TYPE.id();
    }

    default boolean isByPass() {
        return id() == BY_PASS.id();
    }

    static boolean isNoType(byte id) {
        return id == NO_TYPE.id();
    }

    static boolean isByPass(byte id) {
        return id == BY_PASS.id();
    }

    /**
     * For Test only.
     *
     * @return
     */
    static MessageType newType(byte id) {
        return new DefaultMessageType(id);
    }

    static MessageType newType(int id) {
        return new DefaultMessageType((byte) id);
    }

    static MessageType newType() {
        return new DefaultMessageType((byte) 0);
    }

    default int compareTo(T o) {
        if (o == null) return 1;
        if (this.id() == o.id()) return 0;
        if (this.id() < o.id()) return -1;
        return 1;
    }

    default void checkMessageTypes(T[] messageTypes) {

        Arrays.sort(messageTypes);

        MessageType lastMessageType = null;
        for (int i = 0; i < messageTypes.length; i++) {
            if (messageTypes[i].id() < 0 || messageTypes[i].id() > MAX_USER_ID) {
                throw new UnsupportMessageTypeException("MessageType id should be in range [0,23]: " + messageTypes[i]);
            }
            if (lastMessageType != null && messageTypes[i].id() == lastMessageType.id()) {
                throw new UnsupportMessageTypeException("Same MessageType id: " + messageTypes[i] + ", " + lastMessageType);
            }
            lastMessageType = messageTypes[i];
        }
    }

    @Getter
    @Accessors(fluent = true)
    @AllArgsConstructor
    class DefaultMessageType implements MessageType<DefaultMessageType> {
        private byte id;
    }
}
