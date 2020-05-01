package io.apef.base.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import java.io.*;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

/**
 *
 * @author <a href="mailto:zhuangzhi.liu@thistech.com">Zhuangzhi Liu</a>
 */
public class Util {
    private final static Logger log = LoggerFactory.getLogger(Util.class);

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            log.error("sleep interrupted",e);
        }
    }

    public static boolean skip(ByteBuffer buffer, int l) {
        buffer.position(buffer.position()+l);
        return true ;
    }

    public static boolean back(ByteBuffer buffer, int l) {
        buffer.position(buffer.position()-l);
        return true ;
    }

    public static void closeQuietly(ReadableByteChannel channel) {
        if (channel == null)
            return;
        try {
            channel.close();
        } catch (IOException e) {
            log.error("close error:",e);
        }
    }
    public static String toString(ByteBuffer b) {
        return new String(b.array(), b.position(), b.remaining());
    }

    public static void writeTo(ByteBuffer buffer, File file) throws IOException {
        FileChannel out = null;
        try {
            out = new FileOutputStream(file).getChannel();
            out.write(buffer);
        } finally {
            closeQuietly(out);
        }
    }

    public static byte[] toArray(ByteBuffer buffer) {
        byte[] result = new byte[buffer.remaining()];
        buffer.duplicate().get(result);
        return result;
    }

    public static byte[] toArray(ByteBuffer buffer, int count) {
        byte[] result = new byte[Math.min(buffer.remaining(), count)];
        buffer.duplicate().get(result);
        return result;
    }

    public static void write(ByteBuffer to, ByteBuffer from) {
        if (from.hasArray()) {
            to.put(from.array(), from.arrayOffset() + from.position(), Math.min(to.remaining(), from.remaining()));
        } else {
            to.put(toArray(from, to.remaining()));
        }
    }

    public static void write(ByteBuffer to, ByteBuffer from, int count) {
        if (from.hasArray()) {
            to.put(from.array(), from.arrayOffset() + from.position(), Math.min(from.remaining(), count));
        } else {
            to.put(toArray(from, count));
        }
    }

    public static void fill(ByteBuffer buffer, byte val) {
        while (buffer.hasRemaining())
            buffer.put(val);
    }


    public static int getUByte(ByteBuffer buffer, int pos) {
        return buffer.get(pos) & 0xff;
    }

    public static int getByte(ByteBuffer buffer) {
        return buffer.get() & 0xff;
    }
    public static int getByte(ByteBuffer buffer, int pos) {
        return buffer.get(pos) & 0xff;
    }

    public static int getInt(ByteBuffer buffer,int pos, int l) {
        if (l>4) {
            throw new RuntimeException("Max int is 4 bytes");
        }
        int x = 0 ;
        int to = pos+l;
        while(pos<to) {
            int b = buffer.get(pos++) & 0xff;
            x = (x<<8)|b;
        }
        return x ;
    }

    public static int getInt(byte[] buffer,int pos, int l) {
        if (l>4) {
            throw new RuntimeException("Max int is 4 bytes");
        }
        int x = 0 ;
        int to = pos+l;
        while(pos<to) {
            int b = buffer[pos++] & 0xff;
            x = (x<<8)|b;
        }
        return x ;
    }

    public static int getInt(ByteBuffer buffer,int l) {
        int pos = buffer.position();
        int r = getInt(buffer, pos, l);
        buffer.position(pos+l);
        return r;
    }

    public static long getLong(ByteBuffer buffer,int pos, int l) {
        if (l>8) {
            throw new RuntimeException("Max int is 4 bytes");
        }
        long x = 0 ;
        int to = pos+l;
        while(pos<to) {
            int b = buffer.get(pos++) & 0xff;
            x = (x<<8)|b;
        }
        return x ;
    }
    public static long getLong(byte[] buffer,int pos, int l) {
        if (l>8) {
            throw new RuntimeException("Max int is 4 bytes");
        }
        long x = 0 ;
        int to = pos+l;
        while(pos<to) {
            int b = buffer[pos++] & 0xff;
            x = (x<<8)|b;
        }
        return x ;
    }

    public static long getLong(ByteBuffer buffer,int l) {
        int pos = buffer.position();
        long r = getLong(buffer, pos, l);
        buffer.position(pos+l);
        return r;
    }



    /**
     * Parse port of Netty UDP DiagramPacket
     * @param host /0:0:0:0:0:0:0:0:1234
     * @return port
     */
    public static int getPort(String host) {
        String [] ss = host.split(":");
        int port = Integer.parseInt(ss[ss.length-1]);
        if (port<1000 || port>9999) {
            log.warn("Invalid port:{}",port);
        }
        return port ;
    }

    public static long ptsTime(long v) {
        long pts = 0 ;
        pts |= (v >> 3) & (0x0007 << 30);   // top 3 bits, shifted left by 3, other bits zeroed out
        pts |= (v >> 2) & (0x7fff << 15);   // middle 15 bits
        pts |= (v >> 1) & (0x7fff);         // bottom 15 bits

        return pts;
    }



    public static void adjustTS(ByteBuffer tsBuf) {
        int pos = tsBuf.position();
        tsBuf.position(pos-pos%188);

    }

    public static String getStr(ByteBuffer buf, int len) {
        if (buf.remaining()<len) {
            return null;
        }

        String str = new String (buf.array(), buf.position(), len);
        Util.skip(buf, len);
        return str;
    }

    public static String getStr(ByteBuffer buf) {
        String str = new String (buf.array(), buf.position(), buf.limit());
        skip(buf, buf.remaining());
        return str;
    }

    public static List<String> getLocalIpAddresses() {
        List<String> r = new ArrayList<>();
        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements()) {
                    InetAddress i = (InetAddress) ee.nextElement();
                    String ip = i.getHostAddress();

                    //log.info("IP:{},len:{}",ip,ip.split("\\.").length);
                    if (ip.split("\\.").length>3) {
                        r.add(ip);
                    }
                }
            }
            return r;
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean notEmpty(String s) {
        return s!=null && !s.isEmpty();
    }


    public static boolean equals(byte[] a1, int p1, byte[] a2, int p2, int length) {
        if (a1==a2) {
            return true;
        }
        if (a1==null || a2==null) {
            return false;
        }

        if (a1.length-p1<length || a2.length-p2<length) {
            return false;
        }
        for (int i=0; i<length; i++)
            if (a1[p1+i] != a2[p2+i])
                return false;

        return true;

    }

    public static void memset(byte[] buf, int pos, int value, int length) {
        int l = Math.min(buf.length, pos+length);
        for (int p=pos;p<l;p++) {
            buf[p] = (byte)value;
        }
    }

    public static void memmove(byte[] buf, int to, int from, int length) {
        if (to==from) return ;
        int offset = to - from;
        if (to>from) {
            int p = Math.min(to+length,buf.length) - 1;
            for (;p>=to;p--) {
                buf[p] = buf[p-offset];
            }
        } else {
            int end = Math.min(from+length, buf.length)+offset;
            int p=to;
            for (;p<end;p++) {
                buf[p] = buf[p-offset];
            }
        }
    }

    public static void memcpy(byte[] buf, int to, byte[] buffFrom, int from, int length) {
        if (buf==buffFrom) {
            memmove(buf, to, from, length);
        } else {
            int len = Math.min(length,Math.min(buf.length-to,buffFrom.length-from)) ;
            System.arraycopy(buffFrom, from, buf, to, len);
//            for (int p = 0;p<len;p++) {
//                buf[to+p] = buffFrom[from+p];
//            }
        }
    }

    public static byte[] copyOf(byte[] buf, int from, int length) {
        byte [] nBuf = new byte[length];
        memcpy(nBuf,0,buf,from,length);
        return nBuf;
    }

    public static ByteBuffer cloneBuffer(ByteBuffer cache) {
        ByteBuffer to = ByteBuffer.allocate(cache.remaining());
        to.put(cache.duplicate());
        to.clear();
        return to;
    }

    public static  ByteBuffer read(ByteBuffer buffer, int count) {
        ByteBuffer slice = buffer.duplicate();
        int limit = buffer.position() + count;
        slice.limit(limit);
        buffer.position(limit);
        return slice;
    }

    public static byte getRel(ByteBuffer bb, int rel) {
        return bb.get(bb.position() + rel);
    }

    public static int read(InputStream in, ByteBuffer buffer) throws IOException {
        int v;
        while((v=in.read())>0 && buffer.hasRemaining()) {
            buffer.put((byte)v);
        }
        buffer.flip();
        return buffer.remaining();
    }

    public static int read(InputStream in, byte[] array) throws IOException {
        int pos = 0;
        while(in.available()>0) {
            int len = in.read(array, pos, array.length-pos);
            if (len==array.length-pos) {
                return array.length;
            }
            pos += len;
        }
        return pos;
    }

    public static ByteBuffer loadFile(String file, boolean fromResource) throws IOException{
        InputStream in ;
        if (fromResource) {
            in = getResourceStream(file);
        } else {
            in = new FileInputStream(file);
        }
        int len = in.available();
        byte [] array = new byte[len];
        read(in,array);
        return ByteBuffer.wrap(array);
    }

    public static InputStream getResourceStream(String file) throws IOException {
//        ClassPathResource cpr = new ClassPathResource(file);
//        return cpr.getInputStream();
        InputStream is;
        is = ClassLoader.getSystemResourceAsStream(file);
        return is;
    }

    public static File getResourceFile(String file) throws IOException {
        return new File(ClassLoader.getSystemResource(file).getFile());
    }

    final static Singleton<DatatypeFactory> DF = new Singleton<>(DatatypeFactory::newInstance);



    public  static Long parseDuration(String duration) {
        if (duration==null||duration.isEmpty()) {
            return null;
        }
        Duration d = DF.getInstance().newDuration(duration);
        return d.getTimeInMillis(new Date(0));
    }

    public static String generateDuration(long t) {
        return DF.getInstance().newDuration(t).toString();
    }

    public static Unsafe getUnsafe() {
        Unsafe unsafe =null;
        try{
            Class<?> clazz = Unsafe.class;
            Field f;

            f = clazz.getDeclaredField("theUnsafe");

            f.setAccessible(true);
            unsafe = (Unsafe) f.get(clazz);
        } catch (IllegalAccessException | SecurityException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return unsafe;

    }

    public static ByteBuffer merge(List<ByteBuffer> buffers) {
        int len = 0;
        for (ByteBuffer b : buffers) {
            len += b.remaining();
        }
        ByteBuffer buf = ByteBuffer.allocate(len);
        for (ByteBuffer b : buffers) {
            buf.put(b);
        }

        buf.flip();
        return buf;
    }
}
