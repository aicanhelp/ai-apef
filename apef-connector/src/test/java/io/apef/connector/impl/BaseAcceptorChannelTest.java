package io.apef.connector.impl;

import io.apef.core.APEF;
import io.apef.core.channel.BusinessChannel;
import io.apef.core.channel.ChannelConfig;
import io.apef.core.channel.MessageType;
import io.apef.core.channel.message.ChannelMessageContext;
import io.apef.core.channel.pipe.S2BPipe;
import io.apef.sdef.ApefSdef;
import io.apef.sdef.connector.http.server.VertxHttpServer;
import io.apef.testing.unit.BaseUnitSpec;
import io.apef.base.cache.CacheStats;
import io.apef.base.utils.Bytes;
import io.apef.base.exception.VexExceptions;
import io.apef.connector.acceptor.AcceptorChannel;
import io.apef.connector.sender.SenderChannel;
import io.apef.connector.sender.SenderChannelCache;
import io.apef.connector.sender.SenderChannelPipe;
import io.vertx.core.http.HttpClient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.concurrent.atomic.AtomicInteger;

import static io.apef.testing.benchmark.Benchmark.benchmark;
import static io.apef.base.utils.Bytes.bytesOf;
import static org.testng.Assert.assertEquals;


public abstract class BaseAcceptorChannelTest extends BaseUnitSpec {
    @Getter
    @Accessors(fluent = true)
    @AllArgsConstructor
    protected enum TestRequestType implements MessageType<TestRequestType> {
        R1((byte) 0), R2((byte) 1);
        private byte id;
    }

    private volatile BusinessChannel<?> businessChannel;

    private BusinessChannel<?> initAcceptor(AcceptorChannel<?, ?> acceptorChannel) {
        if (businessChannel != null) return businessChannel;
        MessageType businessType = MessageType.newType();
        businessChannel = APEF.createBusinessChannel(new ChannelConfig()
                .setName("VertxAcceptorChannelOneLevelTest_" + name));
        businessChannel.handler(businessType, ChannelMessageContext::succeed);
        businessChannel.start();

        S2BPipe<?, ?> pipe = businessChannel.S2BPipe(acceptorChannel);

        acceptorChannel
                .<String, String>newChannelContext()
                .withRequestType(TestRequestType.R1)
                .withRequestDecoder((context, data) -> Bytes.readString(data))
                .withResponseEncoder((outContext, out, response) -> out.writeBytes(bytesOf(response)))
                .withRequestHandler(context -> {
                    if (context.request().startsWith("E")) {
                        context.response().fail(context.request(), null);
                        return;
                    }
                    if (context.request().startsWith("R")) {
                        context.response().fail("http://localhost", VexExceptions.E_302.exception());
                        return;
                    }
                    pipe.request()
                            .onSuccess((responseContent) -> {
                                context.response().succeed(responseContent.toString());
                            }).onFailure(log::error)
                            .messageType(businessType)
                            .requestContent(context.request())
                            .end();
                })
                .endChannelContext()
                .<String, String>newChannelContext()
                .withRequestDecoder((context, data) -> Bytes.readString(data))
                .withResponseEncoder((outContext, out, response) -> out.writeBytes(bytesOf(response)))
                .withRequestHandler(context -> {
                    if (context.request().startsWith("E")) {
                        context.response().fail(context.request(), null);
                        return;
                    }
                    if (context.request().startsWith("R")) {
                        context.response().fail("http://localhost", VexExceptions.E_302.exception());
                        return;
                    }
                    pipe.request()
                            .onSuccess((responseContent) -> {
                                context.response().succeed(responseContent.toString());
                            }).onFailure(log::error)
                            .messageType(businessType)
                            .requestContent(context.request())
                            .end();
                })
                .endChannelContext();
        return businessChannel;
    }

    private void initSenderChannel(SenderChannel<?> senderChannel) {
        senderChannel.<String, String, String>newChannelContext()
                .withRequestType(TestRequestType.R1)
                .withRequestEncoder((outContext, out, requestData) -> out.writeBytes(bytesOf(requestData)))
                .withResponseDecoder((inContext, requestData, response) -> Bytes.readString(response))
                .withSenderCache(SenderChannelCache.build(100))
                .withExpireChecker(o -> false)
                .withAffinityKeyMapper(Bytes::byteBufOf)
                .withRequestKeyMapper(object -> object)
                .withResponseKeyMapper(object -> object)
                .endChannelContext();
    }

    protected SenderChannelPipe senderChannelPipeOneLevel(String name,
                                                          AcceptorChannel<?, ?> acceptorChannel,
                                                          SenderChannel<?> senderChannel) {
        BusinessChannel<?> businessChannel = initAcceptor(acceptorChannel);
        initSenderChannel(senderChannel);

        return senderChannel.senderChannelPipe(businessChannel);
    }


    protected void doHttpClientTest(AcceptorChannel<?, ?> acceptorChannel) {
        this.initAcceptor(acceptorChannel);
        HttpClient httpClient = ApefSdef.defaultHttpClient();

        String url = "http://localhost:" + VertxHttpServer.DEFAULT_PORT + "/get";

        Blocker blocker = new Blocker();
        httpClient.postAbs(url)
                .handler(httpClientResponse -> {
                    assertEquals(httpClientResponse.statusCode(), 200);
                    httpClientResponse.bodyHandler(buffer -> {
                        blocker.assertTrue(buffer.toString().contains("123456")).end();
                    });
                })
                .exceptionHandler(throwable -> {
                    log.error("Error on send request", throwable);
                }).end("123456");

        blocker.awaitEnd().reset();

        httpClient.getAbs(url)
                .handler(httpClientResponse -> {
                    assertEquals(httpClientResponse.statusCode(), 200);
                    httpClientResponse.bodyHandler(buffer -> {
                        blocker.assertTrue(buffer.toString().contains("123456")).end();
                    });
                })
                .exceptionHandler(throwable -> {
                    log.error("Error on send request", throwable);
                }).end("123456");

        blocker.awaitEnd();
    }

    protected void doHttpClient302Test(AcceptorChannel<?, ?> acceptorChannel) {
        this.initAcceptor(acceptorChannel);
        HttpClient httpClient = ApefSdef.defaultHttpClient();

        String url = "http://localhost:" + VertxHttpServer.DEFAULT_PORT + "/get";

        Blocker blocker = new Blocker();
        httpClient.postAbs(url)
                .handler(httpClientResponse -> {
                    blocker.assertEquals(httpClientResponse.statusCode(), 302)
                            .assertTrue(httpClientResponse.getHeader("Location")
                                    .contains("http"))
                            .end();
                })
                .exceptionHandler(throwable -> {
                    log.error("Error on send request", throwable);
                }).end("R123456");

        blocker.awaitEnd();
    }

    protected void doHttpClientFailTest(AcceptorChannel<?, ?> acceptorChannel) {
        this.initAcceptor(acceptorChannel);
        HttpClient httpClient = ApefSdef.defaultHttpClient();

        String url = "http://localhost:" + VertxHttpServer.DEFAULT_PORT + "/get";

        Blocker blocker = new Blocker();
        httpClient.postAbs(url)
                .handler(httpClientResponse -> {
                    assertEquals(httpClientResponse.statusCode(), 500);
                    httpClientResponse.bodyHandler(buffer -> {
                        blocker.assertTrue(buffer.toString().contains("123456")).end();
                    });
                })
                .exceptionHandler(throwable -> {
                    log.error("Error on send request", throwable);
                }).end("E123456");

        blocker.awaitEnd().reset();

        httpClient.getAbs(url)
                .handler(httpClientResponse -> {
                    assertEquals(httpClientResponse.statusCode(), 500);
                    httpClientResponse.bodyHandler(buffer -> {
                        blocker.assertTrue(buffer.toString().contains("123456")).end();
                    });
                })
                .exceptionHandler(throwable -> {
                    log.error("Error on send request", throwable);
                }).end("E123456");

        blocker.awaitEnd();
    }

    protected void doAcceptorSenderTest(String name, AcceptorChannel<?, ?> acceptorChannel,
                                        SenderChannel<?> senderChannel) {

        SenderChannelPipe senderChannelPipe = senderChannelPipeOneLevel(name, acceptorChannel, senderChannel);
        Blocker blocker = new Blocker();
        senderChannelPipe
                .send(TestRequestType.R1)
                .requestContent("12345678")
                .onSuccess((responseContent) -> {
                    blocker.assertEquals("12345678", responseContent).end();
                })
                .onFailure((errMsg, cause) -> {
                    blocker.failAndEnd("should be success:" + errMsg, cause);
                })
                .end();
        blocker.awaitEnd().reset();
        AtomicInteger counter = new AtomicInteger();
        for (int i = 0; i < 10; i++) {
            senderChannelPipe
                    .send(TestRequestType.R1)
                    .requestContent("12345678")
                    .onSuccess((responseContent) -> {
                        blocker.assertEquals("12345678", responseContent);
                        blocker.endIf(counter.incrementAndGet() == 10);
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker.failAndEnd("should be success:" + errMsg, cause);
                    })
                    .end();
        }

        blocker.awaitEnd();

        CacheStats cacheStats = senderChannel.
                contextManager().channelContext(TestRequestType.R1.id()).cacheStats();

        assertEquals(cacheStats.hitCount(), 10);
        assertEquals(cacheStats.missCount(), 1);
    }

    protected void doFailTest(String name, AcceptorChannel<?, ?> acceptorChannel,
                              SenderChannel<?> senderChannel) {
        SenderChannelPipe senderChannelPipe = senderChannelPipeOneLevel(name + "testFail", acceptorChannel, senderChannel);

        //test failed
        Blocker blocker = new Blocker();
        senderChannelPipe
                .send(TestRequestType.R1)
                .requestContent("E12345678")
                .onSuccess((responseContent) -> {
                    blocker.failAndEnd("should be failure");
                })
                .onFailure((errMsg, cause) -> {
                    blocker.assertTrue(errMsg.contains("E12345678")).end();
                })
                .end();
        blocker.awaitEnd();
    }

    protected void do302Test(String name, AcceptorChannel<?, ?> acceptorChannel,
                             SenderChannel<?> senderChannel) {
        SenderChannelPipe senderChannelPipe = senderChannelPipeOneLevel(name + "Test302", acceptorChannel, senderChannel);

        //test 302
        Blocker blocker = new Blocker();
        senderChannelPipe
                .send(TestRequestType.R1)
                .requestContent("R12345678")
                .onSuccess((responseContent) -> {
                    blocker.failAndEnd("should be failure");
                })
                .onFailure((errMsg, cause) -> {
                    blocker.assertTrue(errMsg.startsWith("http")).end();
                })
                .end();
        blocker.awaitEnd();
    }

    protected void doBaseBenchmark(String name, AcceptorChannel<?, ?> acceptorChannel,
                                   SenderChannel<?> senderChannel) {
        SenderChannelPipe senderChannelPipe = senderChannelPipeOneLevel(name + "ChannelPipe",
                acceptorChannel, senderChannel);

        benchmark()
                .warmupConcurrency(10)
                .warmupIterations(100)
                .warmupRounds(5)
                .concurrency(1000)
                .iterations(30)
                .rounds(10)
                .async(true)
                .benchmarkTask((index, runnerContext) -> {
                    senderChannelPipe
                            .send(TestRequestType.R1)
                            .requestContent("12345678_" + index)
                            .onSuccess((responseContent) -> {
                                runnerContext.done(index);
                            })
                            .onFailure((errMsg, cause) -> {
                                log.error("Benchmark error: " + errMsg);
                                runnerContext.done(index);
                            })
                            .end();
                }).start();
    }
}
