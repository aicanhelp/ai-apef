package io.apef.connector.base;

import io.apef.core.channel.MessageType;
import io.apef.base.utils.TransactionId;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TxInf {
    private final static short UNDO_CODE = -1;
    private final static short OK_CODE = 200;
    public final static TxInf EMPTY = new TxInf();
    private final static TxInf INVALID = new TxInf() {
        @Override
        public boolean isValid() {
            return false;
        }
    };
    private int transactionId;
    private byte requestType = MessageType.BY_PASS.id();
    @Setter
    @Accessors(fluent = true)
    private short statusCode = UNDO_CODE;
    @Setter
    @Accessors(fluent = true)
    private boolean success = false;

    private TxInf() {
    }

    public static TxInf newTxInf(byte requestType) {
        if (MessageType.isByPass(requestType)) return EMPTY;
        return new TxInf(TransactionId.next(), requestType, UNDO_CODE, false);
    }

    public static TxInf from(byte requestType, int transactionId, short statusCode) {
        return new TxInf(transactionId, requestType, statusCode, false);
    }

    public TxInf success(boolean success, short statusCode) {
        this.success = success;
        if (success)
            this.statusCode = statusCode;

        return this;
    }

    public static TxInf from(ByteBuf data) {
        if (data == null) return EMPTY;
        byte requestType = data.readByte();
        int transactionId = data.readInt();
        boolean success = data.readBoolean();
        short statusCode = data.readShort();
        return new TxInf(transactionId, requestType, statusCode, success);
    }

    public static TxInf from(String txInfString) {
        if (StringUtils.isEmpty(txInfString))
            return EMPTY;
        String[] values = txInfString.split(",");
        TxInf txInf = new TxInf();
        try {
            txInf.requestType = Byte.valueOf(values[0]);
            if (values.length > 1)
                txInf.transactionId = Integer.valueOf(values[1]);
            if (values.length > 2)
                txInf.success = Boolean.valueOf(values[2]);
            if (values.length > 3)
                txInf.statusCode = Short.valueOf(values[3]);
        } catch (Exception ex) {
            return INVALID;
        }

        return txInf;
    }

    public void emitTo(ByteBuf out) {
        out.writeByte(this.requestType)
                .writeInt(this.transactionId)
                .writeBoolean(this.success)
                .writeShort(this.statusCode);
    }

    public boolean isValid() {
        return true;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public static boolean isEmpty(TxInf txInf) {
        return txInf == null || txInf == EMPTY;
    }

    public String toString() {
        return requestType + "," + transactionId + "," + success + "," + statusCode;
    }

    public static TxInf from(String requestType, String transactionId) {
        int txid = 0;
        byte type = MessageType.BY_PASS.id();
        if (transactionId != null) {
            try {
                txid = Integer.parseInt(transactionId);
            } catch (Exception ex) {

            }
        }
        if (requestType != null) {
            try {
                type = Byte.parseByte(requestType);
            } catch (Exception ex) {

            }
        }
        return from(type, (short) txid, UNDO_CODE);
    }
}
