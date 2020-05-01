package io.apef.sdef.connector.tcp;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter(AccessLevel.PROTECTED)
@Getter
@Accessors(fluent = true)
public class TcpClientResponse {
    private int txId;
    private ByteBuf responseData;
    private Throwable ex;

    public TcpClientResponse(ByteBuf responseData) {
        this.responseData = responseData;
    }

    public TcpClientResponse(Throwable ex) {
        this.ex = ex;
    }

    private TcpClientResponse(int txId, Throwable ex) {
        this.txId = txId;
        this.ex = ex;
    }

    public boolean isSuccess() {
        return this.ex == null;
    }

    public boolean isFailOnClient() {
        return false;
    }

    public static TcpClientResponse failOnClient(int txId, Throwable ex) {
        return new TcpClientResponse(txId, ex) {
            @Override
            public boolean isFailOnClient() {
                return true;
            }
        };
    }
}
