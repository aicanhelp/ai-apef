package io.apef.sdef.connector.http;

import io.apef.sdef.ApefSdef;
import io.apef.sdef.connector.http.server.VertxHttpServer;
import io.apef.testing.unit.BaseUnitSpec;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.impl.EventLoopContext;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

import static io.apef.testing.benchmark.Benchmark.benchmark;

@Slf4j
public class VertxConnectorBenchmark extends BaseUnitSpec {
    @Test
    public void benchmarkGeneral() {
        Vertx vertx = ApefSdef.vertx();
        VertxHttpServer httpServer = ApefSdef.defaultHttpServer();
        HttpClient httpClient = ApefSdef.defaultHttpClient();
        httpServer.getHandler("/get1", event -> {
            event.response()
                    .end("OK");
        });

        String url = "http://localhost:" + VertxHttpServer.DEFAULT_PORT + "/get1";

        benchmark()
                .warmupConcurrency(10)
                .warmupIterations(5)
                .warmupRounds(5)
                .concurrency(100)
                .iterations(100)
                .rounds(5)
                .benchmarkTask((index, runnerContext) -> {
                    HttpClientRequest request = httpClient.getAbs(url);
                    request.handler(event -> {
                        runnerContext.done(index);
                    }).exceptionHandler(event -> {
                        log.error("Exception on get level 1", event);
                    }).end();

                }).start();
        httpServer.close();
        httpClient.close();
    }

    public static class VHttpClient {
        private ThreadLocal<HttpClient> threadLocal;
        private HttpClient current;

        public VHttpClient() {
            this.threadLocal = ThreadLocal.withInitial(() -> {
                return Vertx.vertx().createHttpClient();
            });
        }

        public HttpClient httpClient() {
            if (EventLoopContext.isOnVertxThread()) {
                return current;
            }


            this.current = threadLocal.get();
            return current;
        }
    }

    @Test
    public void benchmarkRedirect() {
        String url = "http://localhost:" + VertxHttpServer.DEFAULT_PORT + "/get1";
        String url2 = "http://0.0.0.0:8097/get2";
        Vertx vertx = ApefSdef.vertx();
        VertxHttpServer httpServer = ApefSdef.defaultHttpServer();
        VertxHttpServer httpServer2 = ApefSdef.httpServer("manifestConnector");
        HttpClient httpClient = ApefSdef.defaultHttpClient();
        httpServer.getHandler("/get1", event -> {
            for (int i = 0; i < 10000; i++) {
                String a = "fjdlfdll" + System.currentTimeMillis();
            }
            event.response().putHeader("Location", url2)
                    .setStatusCode(302).end("OK");
        });
        httpServer2.getHandler("/get2", event -> {
            for (int i = 0; i < 10000; i++) {
                String a = "fjdlfdll" + System.currentTimeMillis();
            }
            event.response().end("OK2");
        });

        for (int j = 0; j < 50; j++) {
            Blocker blocker = new Blocker();
            HttpClientRequest request = httpClient.getAbs(url);
            request.handler(event -> {
                event.bodyHandler(event1 -> {
                    httpClient.getAbs(url2).handler(e -> {
                        e.bodyHandler(event2 -> {
                            blocker.assertEquals(event2.toString(), "OK2").end();
                        });
                    }).end();
                });
            }).end();
            blocker.awaitEnd();
        }

        Blocker b = new Blocker();
        for (int i = 0; i < 3; i++) {
//            new Thread(
//                    () -> {
            for (int j = 0; j < 20; j++) {
                Blocker blocker = new Blocker();
                HttpClientRequest request = httpClient.getAbs(url);
                request.handler(event -> {
                    event.bodyHandler(event1 -> {
                        httpClient.getAbs(url2).handler(e -> {
                            b.timeInc().endIf(b.times() >= 30);
                            e.bodyHandler(event2 -> {
                                blocker.assertEquals(event2.toString(), "OK2").end();
                            });
                        }).end();
                    });
                }).end();
                blocker.awaitEnd();
            }
//                    }
//            ).start();
        }

        b.awaitEnd();
//        for (int i = 0; i < 100; i++) {
//            Blocker blocker = new Blocker();
//
//            httpClient.getAbs(url)
//                    .setFollowRedirects(true)
//                    .handler(event -> {
//                        blocker.end();
//                    })
//                    .end();
//
//            blocker.awaitEnd();
//        }
    }

    @Test
    public void benchmarkTwoLevelCall() {
        VertxHttpServer httpServer = ApefSdef.defaultHttpServer();
        HttpClient httpClient = ApefSdef.defaultHttpClient();
        HttpClient httpClient2 = ApefSdef.httpClient("cdnClientConnector");

        httpServer.getHandler("/get1", event -> {
            event.response().end("OK");
        });

        BaseUnitSpec.Blocker blocker = new BaseUnitSpec.Blocker();
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
        httpServer.close();
    }
}
