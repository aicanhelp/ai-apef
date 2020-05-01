package io.apef.base.utils;

import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class ExpirationTest extends BaseUnitSpec {
    @Override
    protected void doBeforeClass() {
        super.doBeforeClass();
        //for init
        new Expiration(1, 0);
    }

    @Test
    public void testExpiration() throws InterruptedException {
        Expiration expiration = new Expiration(1, 0);

        int expirationSec = expiration.expirationSecs();
        Thread.sleep(2000);

        assertTrue(Expiration.isExpired(expirationSec));
        Expiration expiration2 = new Expiration(3, 0);

        int expirationSec2 = expiration2.expirationSecs();
        Thread.sleep(1000);
        assertFalse(Expiration.isExpired(expirationSec2));
    }
}