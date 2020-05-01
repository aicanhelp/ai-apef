package io.apef.connector.impl.tcp;

import io.apef.sdef.ApefSdef;
import io.apef.sdef.connector.tcp.ITcpContext;
import io.apef.sdef.connector.tcp.ITcpServer;
import io.apef.connector.acceptor.AcceptorConnector;
import io.apef.connector.acceptor.AcceptorRequestContext;
import io.apef.connector.base.TxInf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TcpAcceptorConnector extends AcceptorConnector<ITcpContext, ITcpContext> {
    private TcpAcceptorChannelConfig config;
    private ITcpServer tcpServer;

    public TcpAcceptorConnector(TcpAcceptorChannelConfig channelConfig) {
        super(channelConfig.getName());
        this.config = channelConfig;
        this.tcpServer = ApefSdef.tcpServer(channelConfig.getTcpServer());
        this.tcpServer.onAccept(tcpContext -> {
            dispatch(tcpContext, tcpContext);
        });
        tcpServer.start();
    }

    @Override
    protected <T, R> AcceptorRequestContext<ITcpContext, ITcpContext, T, R> newRequestContext(ITcpContext inContext, ITcpContext outContext) {
        return new AcceptorRequestContext<ITcpContext, ITcpContext, T, R>(inContext, outContext) {
            ByteBuf out = Unpooled.buffer();

            private ByteBuf out() {
                if (out == null) out = Unpooled.buffer();
                return out;
            }

            @Override
            protected TxInf readTxInf() {
                return TxInf.from(inContext.request());
            }

            @Override
            protected ByteBuf readRequestData() {
                return inContext.request();
            }

            @Override
            protected void writeRxInf() {
                txInf().emitTo(out());
            }

            @Override
            protected void writeResponseData(ByteBuf data) {
                out().writeBytes(data);
                outContext.response(out);
            }
        };
    }
}
