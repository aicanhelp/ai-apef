package io.apef.connector.base;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.experimental.Accessors;

public abstract class ConnectorReader<IC> {
    @Getter
    @Accessors(fluent = true)
    private IC inContext;
    private TxInf txInf;
    private ByteBuf data;

    protected ConnectorReader(IC inContext) {
        this.inContext = inContext;
    }

    public TxInf txInf() {
        if (this.txInf != null) return this.txInf;
        if (this.inContext != null) this.txInf = this.readTxInf();
        return this.txInf;
    }

    public ByteBuf data() {
        if (this.data != null) return this.data;
        if (this.inContext != null) this.data = this.readData();
        return this.data;
    }


    protected abstract ByteBuf readData();

    protected abstract TxInf readTxInf();
}
