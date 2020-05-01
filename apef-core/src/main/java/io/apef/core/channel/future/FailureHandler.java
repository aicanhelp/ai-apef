package io.apef.core.channel.future;

public interface FailureHandler {
    void handle(String errMsg, Throwable cause);
}
