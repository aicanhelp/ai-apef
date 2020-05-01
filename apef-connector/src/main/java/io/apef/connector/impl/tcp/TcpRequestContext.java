package io.apef.connector.impl.tcp;

import io.apef.sdef.connector.tcp.ITcpClient;
import io.apef.sdef.connector.tcp.TcpClientResponse;
import io.apef.connector.base.TxInf;
import io.apef.connector.sender.ConnectorRequestContext;
import io.apef.connector.sender.SenderRequestContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.Timeout;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(fluent = true)
public class TcpRequestContext extends ConnectorRequestContext<TcpRequestContext> {
    private final static Exception SEND_EX = new Exception("Failure on write request data");
    private Timeout timeout;
    private ITcpClient tcpClient;
    private TxInf txInf;
    private ByteBuf dataBuf;

    public TcpRequestContext(ITcpClient tcpClient,
                             SenderRequestContext<TcpRequestContext, ?, ?, ?> requestContext) {
        super(requestContext);
        this.tcpClient = tcpClient;
        this.dataBuf = Unpooled.buffer();
    }

    protected void HandleResponse(TxInf txInf, TcpClientResponse response) {
        this.txInf = txInf;
        this.dataBuf = response.responseData();
        if (!response.isSuccess()) {
            requestContext().fail(response.ex().getMessage(),
                    response.ex());
        } else {
            this.requestContext().handleResponse(this);
        }
    }

    @Override
    protected void writeTxInf(TxInf txInf) throws Exception {
        txInf.emitTo(dataBuf);
    }

    @Override
    protected void writeRequestData(ByteBuf data) throws Exception {
        dataBuf.writeBytes(data);
        if (!tcpClient.send(requestContext().affinityKey(),
                requestContext().txInf().transactionId(), dataBuf)) {
            requestContext().fail("Failure on write request data", SEND_EX);
        }
    }

    @Override
    protected TxInf readResponseTxInf() throws Exception {
        //the txInf is set by the method HandleResponse
        if (this.txInf != null) return txInf;
        return TxInf.from(dataBuf);
    }

    @Override
    protected ByteBuf readResponseData() throws Exception {
        return dataBuf;
    }
}
