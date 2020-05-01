package io.apef.sdef.connector.tcp;


import io.apef.testing.unit.BaseUnitSpec;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.*;
import io.vertx.test.core.TestUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import static io.apef.testing.benchmark.Benchmark.benchmark;

public class VertxTcpBenchmark extends BaseUnitSpec {
    private Vertx vertx = Vertx.vertx();
    private NetServer server;
    private NetClient client;

    @BeforeClass
    public void setUp() throws Exception {
        super.beforeClass();
        client = vertx.createNetClient(new NetClientOptions().setConnectTimeout(1000));
        server = vertx.createNetServer(new NetServerOptions().setPort(23456).setHost("localhost"));
    }

    protected void awaitClose(NetServer server) throws InterruptedException {
        BaseUnitSpec.Blocker blocker = new BaseUnitSpec.Blocker();
        server.close((asyncResult) -> {
            blocker.end();
        });
        blocker.awaitEnd();
    }

    @AfterClass
    protected void tearDown() throws Exception {
        if (client != null) {
            client.close();
        }
        if (server != null) {
            awaitClose(server);
        }
    }

    private void startEchoServer(Handler<AsyncResult<NetServer>> listenHandler) {
        Handler<NetSocket> serverHandler = socket -> socket.handler(socket::write);
        server.connectHandler(serverHandler).listen(listenHandler);
    }

    private void testEcho(int length) {
        Buffer sent = TestUtils.randomBuffer(length);
        Blocker blocker = new Blocker();
        Handler<AsyncResult<NetSocket>> clientHandler = (asyncResult) -> {
            if (asyncResult.succeeded()) {
                NetSocket sock = asyncResult.result();
                benchmark()
                        .benchmarkTask((index, runnerContext) -> {
                            Buffer buff = Buffer.buffer();
                            sock.handler((buffer) -> {
                                buff.appendBuffer(buffer);
                                if (buff.length() == length) {
                                    blocker.assertEquals(sent, buff).end();
                                }
                                if (buff.length() > length) {
                                    blocker.failAndEnd("Too many bytes received");
                                }
                            });
                            sock.write(sent);
                        })
                        .start();
            } else {
                blocker.failAndEnd("failed to connect");
            }
        };
        startEchoServer(s -> client.connect(1234, "localhost", clientHandler));
        blocker.awaitEnd();
    }
}
