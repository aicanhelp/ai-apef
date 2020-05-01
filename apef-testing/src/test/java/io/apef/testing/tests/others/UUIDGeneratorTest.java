package io.apef.testing.tests.others;


import io.apef.testing.unit.BaseUnitSpec;
import com.eaio.uuid.UUID;
import com.fasterxml.uuid.impl.UUIDUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.AppendableCharSequence;
import org.testng.annotations.Test;

import java.nio.charset.Charset;

public class UUIDGeneratorTest extends BaseUnitSpec {
    @Test
    public void testEaioUUID() {
        log.info("1----" + new UUID());
        StringBuffer stringBuffer = new StringBuffer();
        new UUID().toAppendable(stringBuffer);
        log.info("2----" + stringBuffer.toString());
        AppendableCharSequence appendableCharSequence = new AppendableCharSequence(36);
        UUID uuid = new UUID();
        log.info("3-------------" + uuid);
        uuid.toAppendable(appendableCharSequence);

        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeCharSequence(appendableCharSequence, Charset.defaultCharset());
        byte[] data = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(data);
        log.info(data.length + "----4------" + new String(data));

        log.info("5---------" + UUIDUtil.uuid(new String(data)));
    }
}
