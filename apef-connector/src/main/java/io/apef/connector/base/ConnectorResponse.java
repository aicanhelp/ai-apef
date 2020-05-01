package io.apef.connector.base;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import static io.apef.base.utils.Bytes.bytesOf;

@Getter
@Setter(AccessLevel.PROTECTED)
@Accessors(fluent = true)
@ToString
public abstract class ConnectorResponse<R> {
    public final static byte[] UNKNOWN_ERROR = bytesOf("Unknown Error");
    private boolean success;
    private int statusCode=200;
    private String errMsg;
    private transient Throwable ex;
    private transient R response;

    protected ConnectorResponse() {
    }
}
