package io.apef.base.utils;

import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;

import java.nio.charset.Charset;
import java.util.UUID;

import static org.testng.Assert.*;

public class UUIDGeneratorTest extends BaseUnitSpec {
    @Test
    public void testFromUUIDFromString() {

        UUID uuid = UUIDGenerator.uuid();
        String uuidString = uuid.toString();
        UUID uuid2 = UUIDGenerator.uuid(uuidString);
        assertEquals(uuid, uuid2);
    }

    @Test
    public void testFromUUIDString() {
        String uuidString = UUIDGenerator.uuidString();

        UUID uuid = UUID.fromString(uuidString);
        UUID uuid1 = UUIDGenerator.uuid(uuidString);

        assertEquals(uuid, uuid1);
        assertEquals(uuidString, uuid1.toString());
    }

    @Test
    public void testUUIDBytesWithDefaultCharset() {
        byte[] uuidBytes = UUIDGenerator.uuidBytes();
        UUID uuid = UUIDGenerator.uuid(uuidBytes);
        UUID uuid1 = UUID.fromString(new String(uuidBytes));
        assertEquals(uuid, uuid1);
    }

    @Test
    public void testUUIDBytesWithCustomCharset() {
        byte[] uuidBytes = UUIDGenerator.uuidBytes(Charset.forName("UTF-8"));
        UUID uuid = UUIDGenerator.uuid(uuidBytes);
        UUID uuid1 = UUID.fromString(new String(uuidBytes, Charset.forName("UTF-8")));
        assertEquals(uuid, uuid1);
    }
}