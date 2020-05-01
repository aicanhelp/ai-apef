package io.apef.base.utils;

import io.netty.buffer.ByteBuf;

import static io.apef.base.utils.Bytes.boolOf;
import static io.apef.base.utils.Bytes.intOf;
import static io.apef.base.utils.Bytes.longOf;

public class CSVReader {
    private final static byte C = (byte) ',';
    private ByteBuf data;
    private byte separator = C;
    private int lastIndex = 0;
    private int total;

    public CSVReader(ByteBuf data) {
        this.data = data;
        this.lastIndex = data.readerIndex();
        this.total = data.readableBytes();
    }

    public CSVReader(char separator, ByteBuf data) {
        this.data = data;
        this.separator = (byte) separator;
    }

    private byte[] next() {
        int index = lastIndex;
        while (index < total) {
            if (this.data.getByte(index++) == this.separator) {
                int len = index - lastIndex;
                byte[] data = null;
                if (len > 1) {
                    data = Bytes.readBytes(this.data, len - 1);
                }
                lastIndex = index;
                this.data.readerIndex(index);
                return data;
            }
        }
        return Bytes.readBytes(this.data);
    }

    public String readString() {
        byte[] data = next();
        if (data == null) {
            return "";
        }
        return Bytes.stringOf(data);
    }

    public int readInt() {
        return intOf(next());
    }

    public long readLong() {
        return longOf(next());
    }

    public boolean readBool() {
        return boolOf(next());
    }
}
