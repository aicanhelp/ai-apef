package io.apef.base.emitter;

import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;


public class BufferEmitterTest extends BaseUnitSpec {
    @Test
    public void testAppend() {
        byte[] data = "123456789".getBytes();
        BufferEmitter bufferEmitter = new BufferEmitter();
        bufferEmitter.append(data, data.length, 0);
    }
}