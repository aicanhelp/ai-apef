package io.apef.sdef.connector.tcp;


public interface ITcpServer {
    void start();

    void close();

    void onAccept(OnAccept onAccept);

    interface OnAccept {
        void accept(ITcpContext tcpContext);
    }
}
