package io.apef.connector.sender;

import io.apef.connector.base.ConnectorWriter;
import io.apef.connector.base.TxInf;
import io.netty.buffer.ByteBuf;

public abstract class SenderWriter<OC> extends ConnectorWriter<OC> {
    protected SenderWriter(OC outContext) {
        super(outContext);
    }

    protected abstract void writeTxInf(TxInf txInf);

    protected abstract void writeData(ByteBuf data);
}
