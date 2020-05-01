package io.apef.connector.impl.vertx;

import io.apef.connector.impl.BaseAcceptorChannelTest;
import io.apef.sdef.connector.http.server.VertxHttpServer;
import org.testng.annotations.Test;

@Test
public class VertxAcceptorChannelTest extends BaseAcceptorChannelTest {
    VertxAcceptorChannel acceptorChannel =
            new VertxAcceptorChannel(new VertxAcceptorChannelConfig()
                    .setName("TestAcceptorChannel")
                    .setContext("/get")
            );

    VertxSenderChannel senderChannel =
            new VertxSenderChannel(new VertxSenderChannelConfig()
                    .setName("TestSenderChannel")
                    .setPost(true)
                    .setServiceAddress("http://localhost:" + VertxHttpServer.DEFAULT_PORT + "/get")
            );


    public void testFunctions() {
        doAcceptorSenderTest("vertx", acceptorChannel, senderChannel);
    }

    public void testFail() {
        doFailTest("vertx", acceptorChannel, senderChannel);
    }

    public void test302() {
        do302Test("vertx", acceptorChannel, senderChannel);
    }

    public void testHttpClient() {
        doHttpClientTest(acceptorChannel);
    }

    public void testHttpClientFail() {
        doHttpClientFailTest(acceptorChannel);
    }

    public void testHttpClient302() {
        doHttpClient302Test(acceptorChannel);
    }
}