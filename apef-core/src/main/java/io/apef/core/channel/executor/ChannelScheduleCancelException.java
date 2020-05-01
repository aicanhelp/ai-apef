package io.apef.core.channel.executor;


public class ChannelScheduleCancelException extends Exception {
    private final static String MSG = "ChannelSchedule Canceled";

    public ChannelScheduleCancelException() {
        super(MSG);
    }

    public ChannelScheduleCancelException(String msg) {
        super(msg);
    }
}
