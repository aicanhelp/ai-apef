package io.apef.core.channel.future;

public interface SuccessHandler<O> {
    void handle(O outputValue);
}
