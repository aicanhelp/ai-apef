package io.apef.base.utils;

import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

@Test
public class ActiveCheckerTest extends BaseUnitSpec {

    public void testBaseActiveFunction() {
        ActiveChecker activeCounter = new ActiveChecker((short) 3);

        for (int i = 0; i < 3; i++)
            assertTrue(activeCounter.isActive());

        assertFalse(activeCounter.isActive());

        activeCounter.update();
        assertTrue(activeCounter.isActive());
    }

    public void testSpecialUpdate() {
        ActiveChecker activeCounter = new ActiveChecker((short) 3);
        for (int i = 0; i < 65536; i++) {
            activeCounter.update();
        }

        assertTrue(activeCounter.isActive());
        for (int i = 0; i < 3; i++)
            assertTrue(activeCounter.isActive());

        assertFalse(activeCounter.isActive());
    }
}