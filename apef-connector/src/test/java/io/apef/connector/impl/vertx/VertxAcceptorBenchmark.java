package io.apef.connector.impl.vertx;


import io.apef.sdef.connector.http.server.VertxHttpServer;
import io.apef.connector.impl.BaseAcceptorChannelTest;
import org.testng.annotations.Test;

public class VertxAcceptorBenchmark extends BaseAcceptorChannelTest {
    @Test
    public void benchmarkOneLevel() {
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
        //TPS: ~ 40k
        doBaseBenchmark("vertx", acceptorChannel, senderChannel);
    }
}
