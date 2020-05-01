package io.apef.sdef.connector.http;

import io.apef.sdef.ApefSdef;
import io.apef.testing.unit.BaseUnitSpec;
import io.apef.sdef.connector.http.server.VertxHttpServer;
import io.vertx.core.http.HttpClient;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;


public class VertxConnectorFactoryTest extends BaseUnitSpec {
    @Test(enabled = true)
    public void testDefault() {
        //call twice to test the singleton of defaultHttpServer
        VertxHttpServer httpServer = ApefSdef.defaultHttpServer();
        VertxHttpServer httpServer2 = ApefSdef.defaultHttpServer();
        assertEquals(httpServer, httpServer2);
        HttpClient httpClient = ApefSdef.defaultHttpClient();
        HttpClient httpClient2 = ApefSdef.defaultHttpClient();
        assertEquals(httpClient, httpClient2);

        doTest(httpServer, httpClient, VertxHttpServer.DEFAULT_PORT, false);
        httpServer.close();
    }

    @Test
    public void testCallOnCallBack() {
        VertxHttpServer httpServer = ApefSdef.defaultHttpServer();
        HttpClient httpClient = ApefSdef.defaultHttpClient();
        HttpClient httpClient2 = ApefSdef.httpClient("cdnClientConnector");

        httpServer.getHandler("/get1", event -> {
            event.response().end("OK");
        });

        Blocker blocker = new Blocker();
        httpClient.getAbs("http://localhost:" + VertxHttpServer.DEFAULT_PORT + "/get1")
                .handler(event -> {
                    event.bodyHandler(event1 -> {
                        httpClient.getAbs("http://localhost:" + VertxHttpServer.DEFAULT_PORT + "/get1")
                                .handler(event2 -> {
                                    event2.bodyHandler(event3 -> {
                                        blocker.end();
                                    }).exceptionHandler(event3 -> {
                                        log.error("Exception on get level 2 handle body", event2);
                                    });
                                }).exceptionHandler(event2 -> {
                            log.error("Exception on get level 2", event1);
                        }).end();
                    }).exceptionHandler(event1 -> {
                        log.error("Exception on get level 1 handle body", event1);
                    });
                }).exceptionHandler(event -> {
            log.error("Exception on get level 1", event);
        }).end();

        blocker.awaitEnd();
    }

    @Test
    public void testCustom() {
        VertxHttpServer httpServer = ApefSdef.httpServer("manifestConnector");
        HttpClient httpClient = ApefSdef.httpClient("cdnClientConnector");
        doTest(httpServer, httpClient, 8097, false);
        httpServer.close();
    }

    @Test
    public void testSSL() {
        VertxHttpServer httpServer = ApefSdef.httpServer("sslConnector");
        HttpClient httpClient = ApefSdef.httpClient("cdnClientConnector");

        doTest(httpServer, httpClient, 8098, true);
        httpServer.close();
    }

    private void doTest(VertxHttpServer httpServer, HttpClient httpClient, int port, boolean ssl) {
        httpServer.getHandler("/get1", event -> {
            event.response().end("OK");
        });

        Blocker blocker = new Blocker();
        httpClient.getAbs((ssl ? "https" : "http") + "://localhost:" + port + "/get1")
                .handler(event -> {
                    event.bodyHandler(event1 -> {
                        blocker.assertEquals(new String(event1.getBytes()), "OK").end();
                    }).exceptionHandler(event1 -> {
                        blocker.failAndEnd(event1.getMessage());
                    });
                }).exceptionHandler(event -> {
            log.error("Failed on get", event);
        }).end();

        blocker.awaitEnd();
        httpServer.close();
    }
}