package io.apef.connector.sender;


import io.apef.connector.base.TxInf;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@AllArgsConstructor
public abstract class ConnectorRequestContext<C extends ConnectorRequestContext<C>> {
    private SenderRequestContext<C, ?, ?, ?> requestContext;

    public void start(){
        this.requestContext.sendRequest(this);
    }

    protected abstract void writeTxInf(TxInf txInf) throws Exception;

    protected abstract void writeRequestData(ByteBuf data) throws Exception;

    protected abstract TxInf readResponseTxInf() throws Exception;

    protected abstract ByteBuf readResponseData() throws Exception;
}
