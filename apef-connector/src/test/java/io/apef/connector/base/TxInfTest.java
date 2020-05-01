package io.apef.connector.base;

import io.apef.core.channel.MessageType;
import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

@Test
public class TxInfTest extends BaseUnitSpec {
    public void testNewInf() {
        assertTrue(TxInf.from("").isEmpty());
        assertTrue(TxInf.from((String) null).isEmpty());
        assertTrue(TxInf.newTxInf(MessageType.BY_PASS.id()).isEmpty());
    }
}