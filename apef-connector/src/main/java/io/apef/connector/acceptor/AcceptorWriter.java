package io.apef.connector.acceptor;

import io.apef.connector.base.ConnectorWriter;
import io.apef.connector.base.TxInf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import static io.apef.base.utils.Bytes.bytesOf;

public abstract class AcceptorWriter<OC> extends ConnectorWriter<OC> {
    protected AcceptorWriter(OC outContext) {
        super(outContext);
    }

    public void write(int statusCode, TxInf txInf, ByteBuf data) {
        txInf.statusCode((short) statusCode);
        if (!txInf.isEmpty())
            this.writeTxInf(txInf);
        this.writeData(data);
    }

    public void write(int statusCode, TxInf txInf, String data) {
        txInf.statusCode((short) statusCode);
        if (!txInf.isEmpty())
            this.writeTxInf(txInf);
        this.writeData(data == null ? null : Unpooled.wrappedBuffer(bytesOf(data)));
    }

    public abstract void writeTxInf(TxInf txInf);

    public abstract void writeData(ByteBuf data);
}
