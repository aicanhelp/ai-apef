package io.apef.core.channel.feature.timeout;

import io.apef.core.channel.MessageType;

public class TimeoutException extends Exception {
    private final static String TIMEOUT_MSG = "Request Timeout";

    private TimeoutException() {

    }

    public TimeoutException(String msg) {
        super(msg);
    }

    public String message(MessageType messageType, long timeoutMs) {
        return messageType + " " + this.getMessage() + " " + timeoutMs + "ms";
    }

    public final static TimeoutException INSTANCE = new TimeoutException(TIMEOUT_MSG);
}
