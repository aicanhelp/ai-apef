package io.apef.sdef.connector.tcp;


import io.netty.buffer.ByteBuf;

public interface ITcpClient {
    void onResponse(OnResponse onResponse);

    boolean send(ByteBuf key, int txId, ByteBuf data);

    void close();

    interface OnResponse {
        void handle(TcpClientResponse response);
    }
}
