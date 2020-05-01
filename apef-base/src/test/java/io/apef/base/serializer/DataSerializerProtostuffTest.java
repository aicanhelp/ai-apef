package io.apef.base.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.*;

@Slf4j
public class DataSerializerProtostuffTest {

    @Test
    public void testSerialize() throws IOException {
        TestObject testObject = new TestObject();
        testObject.name = "aaaa";

        DataSerializer<TestObject> testSerializer = DataSerializer.protoStuff(TestObject.class);

        TestObject newTestObject = testSerializer.deserialize(testSerializer.serialize(testObject));

        assertEquals(testObject.name, newTestObject.name);
    }

    @Test
    public void testWithByteArrayInput() throws IOException {
        TestObject testObject = new TestObject();
        testObject.name = "aaaa";

        DataSerializer<TestObject> testSerializer = DataSerializer.protoStuff(TestObject.class);
        ByteBuf byteBuf = Unpooled.buffer(16);
        byteBuf.writeInt(99);
        int len=byteBuf.readableBytes();
        testSerializer.serializeTo(testObject, byteBuf);
        byte[] data2 = byteBuf.array();

        ByteBuf byteBuf1 = Unpooled.wrappedBuffer(data2);
        int value = byteBuf1.readInt();
        assertEquals(value, 99);

        TestObject testObject1 = testSerializer.deserialize(data2, len, byteBuf1.readableBytes());

    }


    public static class TestObject {
        String name = "12345";
    }

}