package io.apef.connector.acceptor;

import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;


public class AcceptorResponseTest extends BaseUnitSpec {
    @Test
    public void testToString() {
        AcceptorResponse acceptorResponse = new AcceptorResponse(null);
        log.info("---" + acceptorResponse);
    }
}