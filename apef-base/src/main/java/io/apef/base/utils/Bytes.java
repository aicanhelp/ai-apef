package io.apef.base.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author <a href="mailto:zhuangzhi.liu@thistech.com">Zhuangzhi Liu</a>
 *         Created on: 2016/5/11
 */
@Getter
@Accessors(fluent = true)
public class Bytes {
    public final static Charset CHARSET = Charset.forName("UTF-8");
    public final static String S_CRLF = "\r\n";
    public final static String S_EMPTY = "";
    public final static byte[] BS_CRLF = bytesOf(S_CRLF);
    public final static byte[] BS_EMPTY = bytesOf(S_EMPTY);
    public final static byte C_COLON = ':';
    public final static byte C_COMMA = ',';
    public final static byte C_AT = '@';
    public final static byte C_EQUAL = '=';
    public final static byte C_DQUOTE = '"';
    public final static byte C_POUND = '#';
    public final static byte C_SLASH = '/';
    public final static byte C_MINUS = '-';
    public final static byte C_QMARK = '?';
    public final static byte C_AND = '&';

    //    String data;
    byte[] buf;
    int start;
    int end;
    private int hash; // Default to 0
    private Charset charset = CHARSET;

    public static Bytes wrap(byte[] data) {
        return new Bytes(data);
    }

    public static Bytes wrap(ByteBuf data) {
        if (!data.isDirect()) {
            int start = data.readerIndex();
            int end = start + data.readableBytes();
            return new Bytes(data.array(), start, end);
        } else {
            byte[] bytes = new byte[data.readableBytes()];
            data.getBytes(0, bytes);
            return new Bytes(bytes, 0, bytes.length);
        }
    }

    public static Bytes wrap(ByteBuffer data) {
        return wrap(Unpooled.wrappedBuffer(data));
    }

    public ByteBuf toByteBuf() {
        return Unpooled.wrappedBuffer(buf, start, length());
    }

    public Bytes(String str) {
        this(str.getBytes(CHARSET));
    }

    @Override
    public String toString() {
        if (start == end) return S_EMPTY;
        return new String(buf, start, end - start, charset);
    }

    public String toString(Charset charset) {
        if (start == end) return S_EMPTY;
        return new String(buf, start, end - start, charset);
    }

    /**
     * Returns a hash code for this string. The hash code for a
     * {@code String} object is computed as
     * <blockquote><pre>
     * s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
     * </pre></blockquote>
     * using {@code int} arithmetic, where {@code s[i]} is the
     * <i>i</i>th character of the string, {@code n} is the length of
     * the string, and {@code ^} indicates exponentiation.
     * (The hash value of the empty string is zero.)
     *
     * @return a hash code value for this object.
     */
    public int hashCode() {
        int h = hash;
        if (h == 0 && length() > 0) {
            byte val[] = buf;

            for (int i = start; i < end; i++) {
                h = 31 * h + val[i];
            }
            hash = h;
        }
        return h;
    }

    public Bytes trim() {
        int len = end;
        int st = start;
        byte[] val = buf;    /* avoid getfield opcode */

        while ((st < len) && (val[st] <= ' ')) {
            st++;
        }
        while ((st < len) && (val[len - 1] <= ' ')) {
            len--;
        }
        return ((st > 0) || (len < end)) ? substring(st, len) : this;
    }

    public Bytes(byte data[]) {
        this(data, 0, data.length);
    }

    public Bytes(byte[] buf, int start, int end) {
        this.buf = buf;
        this.start = start;
        this.end = end;
    }

    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof Bytes) {
            Bytes anotherString = (Bytes) anObject;
            int n = length();
            if (n == anotherString.length()) {
                byte v1[] = buf;
                byte v2[] = anotherString.buf;
                int i1 = start;
                int i2 = anotherString.start;
                while (n-- != 0) {
                    if (v1[i1] != v2[i2])
                        return false;
                    i1++;
                    i2++;
                }
                return true;
            }
        }
        return false;
    }

    public boolean startsWith(Bytes str) {
        return startsWith(str, start);
    }

    public boolean startsWith(Bytes prefix, int toffset) {
        byte ta[] = buf;
        int to = toffset;
        byte pa[] = prefix.buf;
        int po = prefix.start;
        int pc = prefix.length();
        // Note: toffset might be near -1>>>1.
        if ((toffset < start) || (toffset > end - pc)) {
            return false;
        }
        while (--pc >= 0) {
            if (ta[to++] != pa[po++]) {
                return false;
            }
        }
        return true;
    }


    public int indexOf(byte c) {
        return indexOf(c, start);
    }

    public int indexOf(byte c, int from) {
        return indexOf(c, from, end);
    }

    public int indexOf(byte c, int from, int to) {
        int p = from;
        byte[] b = buf;
        for (; p < to; p++) {
            if (b[p] == c) return p;
        }
        return -1;
    }

    public int indexOf(char c) {
        return indexOf(c, start);
    }

    public int lastIndexOf(char c) {
        return lastIndexOf(c, start);
    }

    public int lastIndexOf(byte c) {
        return lastIndexOf((char) c, start);
    }

    public int indexOf(char c, int from) {
        int p = from;
        int to = end;
        byte[] b = buf;
        for (; p < to; p++) {
            if (b[p] == c) return p;
        }
        return -1;
    }

    public int lastIndexOf(char c, int from) {
        int p = end - 1;
        int to = from - 1;
        byte[] b = buf;
        for (; p > to; p--) {
            if (b[p] == c) return p;
        }
        return -1;
    }

    public Bytes substring(int from, int to) {
        if (from < start) from = start;
        if (to > end) to = end;
        return new Bytes(buf, from, to);
    }

    public Bytes left(int pos) {
        if (pos < start) pos = start;
        if (pos > end) pos = end;
        return new Bytes(buf, start, pos);
    }

    public Bytes right(int pos) {
        if (pos < start) pos = start;
        if (pos > end) pos = end;
        return new Bytes(buf, pos, end);
    }

    public Bytes[] split(char c) {
        int[] indexs = new int[10];
        int num = 0;
        int i = start;
        while ((i = indexOf(c, i)) >= 0) {
            if (num >= indexs.length) {
                indexs = Arrays.copyOf(indexs, indexs.length * 2);
            }
            indexs[num++] = i++;
        }
        if (num > 0) {
            Bytes[] r = new Bytes[num + 1];
            int f;
            int t = 0;
            for (f = start, i = 0; i < num; i++) {
                if (f == indexs[i]) {
                    f = indexs[i] + 1;
                    continue;
                }
                r[t++] = new Bytes(buf, f, indexs[i]);
                f = indexs[i] + 1;
            }
            if (f != end) r[t++] = new Bytes(buf, f, end);
            if (t == num + 1) return r;
            else return Arrays.copyOf(r, t);
        }
        return new Bytes[0];
    }

    public int length() {
        return end - start;
    }

    public ByteBuf appendTo(ByteBuf sb) {
        sb.writeBytes(buf, start, length());
        return sb;
    }


    public int indexOf(byte[] s) {
        return indexOf(s, start);
    }

    public int indexOf(byte[] s, int fromIndex) {
        return indexOf(buf, 0, end,
                s, 0, s.length, fromIndex);
    }

    public Bytes substringBetween(byte[] s, byte[] s1) {
        int id = indexOf(s);
        if (id < 0) return null;
        int id2 = indexOf(s1, id + s.length);
        if (id2 < 0) return null;
        return new Bytes(buf, id + s.length, id2);
    }

    /**
     * Code shared by String and StringBuffer to do searches. The
     * source is the character array being searched, and the target
     * is the string being searched for.
     *
     * @param source       the characters being searched.
     * @param sourceOffset offset of the source string.
     * @param sourceCount  count of the source string.
     * @param target       the characters being searched for.
     * @param targetOffset offset of the target string.
     * @param targetCount  count of the target string.
     * @param fromIndex    the index to begin searching from.
     */
    static int indexOf(byte[] source, int sourceOffset, int sourceCount,
                       byte[] target, int targetOffset, int targetCount,
                       int fromIndex) {
        if (fromIndex >= sourceCount) {
            return (targetCount == 0 ? sourceCount : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetCount == 0) {
            return fromIndex;
        }

        byte first = target[targetOffset];
        int max = sourceOffset + (sourceCount - targetCount);

        for (int i = sourceOffset + fromIndex; i <= max; i++) {
            /* Look for first character. */
            if (source[i] != first) {
                while (++i <= max && source[i] != first) ;
            }

            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                for (int k = targetOffset + 1; j < end && source[j]
                        == target[k]; j++, k++)
                    ;

                if (j == end) {
                    /* Found whole string. */
                    return i - sourceOffset;
                }
            }
        }
        return -1;
    }

    public boolean startsWith(byte[] bytes) {
        return startsWith(new Bytes(bytes));
    }

    public Bytes[] split(byte[] c) {
        int[] indexs = new int[10];
        int num = 0;
        int i = start;
        while ((i = indexOf(c, i)) >= 0) {
            if (num >= indexs.length) {
                indexs = Arrays.copyOf(indexs, indexs.length * 2);
            }
            i += c.length;
            indexs[num++] = i;
        }
        if (num > 0) {
            Bytes[] r = new Bytes[num + 1];
            int f;
            int t = 0;
            for (f = start, i = 0; i < num; i++) {
                if (f == indexs[i]) {
                    f = indexs[i] + c.length;
                    continue;
                }
                r[t++] = new Bytes(buf, f, indexs[i]);
                f = indexs[i] + c.length;
            }
            if (f != end) r[t++] = new Bytes(buf, f, end);
            if (t == num + 1) return r;
            else return Arrays.copyOf(r, t);
        }
        return new Bytes[0];
    }

    public boolean equals(byte[] a) {
        if (a == null)
            return false;

        int length = a.length;
        if (this.length() != length)
            return false;

        for (int i = 0; i < length; i++)
            if (a[i] != buf[start + i])
                return false;

        return true;
    }

    public boolean equals(byte[] a, int offset, int length) {
        if (a == null)
            return false;

        if (this.length() != length)
            return false;

        for (int i = 1; i < length; i++)
            if (a[offset + i] != buf[i])
                return false;

        return true;
    }

    public ByteBuf writeTo(ByteBuf output) {
        return output.writeBytes(buf, start, length());
    }

    public boolean isEmpty() {
        return length() == 0;
    }


    public Bytes copy() {
        return wrap(Arrays.copyOfRange(buf, start, end));
    }

    public byte[] getBytes() {
        if (start == 0 && end == buf.length) return buf;
        return Arrays.copyOfRange(buf, start, end);
    }

    public static boolean isNotEmpty(Bytes b) {
        return !isEmpty(b);
    }

    public static boolean isEmpty(Bytes b) {
        return b == null || b.isEmpty();
    }

    public static Bytes wrap(String s) {
        return new Bytes(s);
    }

    public byte get(int to) {
        return buf[start + to];
    }

    public static byte[] bytesOf(String value) {
        if (value == null) return null;
        return value.getBytes(CHARSET);
    }

    public static byte[] bytesOf(String value, Charset charset) {
        if (value == null) return null;
        return value.getBytes(charset);
    }

    public static int indexOf(byte[] source, byte[] target) {
        return indexOf(0, source.length, source, target);
    }

    public static int indexOf(byte[] array, byte target) {
        return indexOf(0, array.length, array, target);
    }

    public static int indexOf(int from, byte[] array, byte target) {
        return indexOf(from, array.length, array, target);
    }

    public static int indexOf(int from, int end, byte[] array, byte target) {
        for (int i = from; i < end; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOf(int from, byte[] source, byte[] target) {
        return indexOf(from, source.length, source, target);
    }

    public static byte[] bytesBefore(byte[] bytes, byte c) {
        int index = indexOf(bytes, c);
        if (index == -1) return null;
        return Arrays.copyOf(bytes, index + 1);
    }

    public static int indexOf(int from, int to, byte[] source, byte[] target) {
        if (target.length == 0) {
            return 0;
        }

        outer:
        for (int i = from; i < to - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (source[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    public static String stringOf(byte[] value) {
        return value == null ? null : new String(value, CHARSET);
    }

    public static String stringOf(Bytes bytes) {
        if (bytes == null) return null;
        return bytes.toString();
    }

    public static String stringOf(ByteBuf byteBuf) {
        if (byteBuf == null) return null;
        return byteBuf.toString(CHARSET);
    }

    public static Integer intOf(Bytes bytes) {
        if (bytes == null) return null;
        String data = stringOf(bytes);
        return Integer.parseInt(data);
    }

    public static Integer intOf(byte[] bytes) {
        if (bytes == null) return null;
        String data = stringOf(bytes);
        return Integer.parseInt(data);
    }

    public static Long longOf(byte[] bytes) {
        if (bytes == null) return null;
        String data = stringOf(bytes);
        return Long.parseLong(data);
    }

    public static Double doubleOf(byte[] bytes) {
        if (bytes == null) return null;
        String data = stringOf(bytes);
        return Double.parseDouble(data);
    }

    public static Boolean boolOf(byte[] bytes) {
        if (bytes == null) return null;
        String data = stringOf(bytes);
        return Boolean.valueOf(data);
    }

    public static Double doubleOf(Bytes bytes) {
        if (bytes == null) return null;
        String data = stringOf(bytes);
        return Double.parseDouble(data);
    }

    public static String readString(ByteBuf byteBuf) {
        byte[] data = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(data);
        return stringOf(data);
    }

    public static String readString(ByteBuf byteBuf, int len) {
        byte[] data = new byte[len];
        byteBuf.readBytes(data);
        return stringOf(data);
    }

    public static byte[] readBytes(ByteBuf byteBuf) {
        if (byteBuf == null || byteBuf.readableBytes() == 0) return null;
        byte[] data = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(data);
        return data;
    }

    public static byte[] bytes(ByteBuf byteBuf, int from, int to) {
        byte[] data = new byte[to - from];
        byteBuf.getBytes(from, data);
        return data;
    }

    public static byte[] readBytes(ByteBuf byteBuf, int len) {
        byte[] data = new byte[len];
        byteBuf.readBytes(data);
        return data;
    }

    public static byte[] bytes(byte b) {
        byte[] bytes = new byte[1];
        bytes[0] = b;
        return bytes;
    }

    public static ByteBuf byteBufOf(String value) {
        return Unpooled.wrappedBuffer(bytesOf(value));
    }
}
