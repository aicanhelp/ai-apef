package io.apef.core.channel;

import io.apef.core.APEF;
import io.apef.core.channel.pipe.*;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;

public class ClientChannelTest extends ChannelTest {
    @Test
    public void testB2CNoReply() {

        ClientChannel channel = APEF.createClientChannel(new ChannelConfig().setName("client"));
        B2CNoReplyPipe<?> noReplyPipe = channel.B2CNoReplyPipe();

        super.testNoReply(noReplyPipe);
    }

    @Test(expectedExceptions = {Exception.class})
    public void testB2CNoReplyFutureException() {
        BusinessChannel businessChannel = APEF.createBusinessChannel(new ChannelConfig().setName("testB2CNoReplyFutureException"))
                .start();
        ClientChannel clientChannel = APEF.createClientChannel(new ChannelConfig().setName("client"));
        B2CPipe<?, ?> pipe = clientChannel.B2CPipe(businessChannel);
        //noReply can not create future
        pipe.request()
                .noReply()
                .onSuccess((responseContent) -> {
                })
                .onFailure((errMsg, cause) -> {
                })
                .future();
    }

    @Test
    public void testB2CFuture() {
        MessageType messageType = MessageType.newType();
        BusinessChannel businessChannel = APEF.createBusinessChannel(new ChannelConfig().setName("testB2CFuture"))
                .start();
        ClientChannel clientChannel = APEF.createClientChannel(new ChannelConfig().setName("client"));
        clientChannel.handler(messageType, (messageContext, requestContent) -> {
            messageContext.succeed(requestContent);
        }).start();
        B2CPipe<?, ?> pipe = clientChannel.B2CPipe(businessChannel);

        Blocker blocker1 = new Blocker();
        Blocker blocker2 = new Blocker();
        String content = "test";
        pipe.request()
                .requestContent("test")
                .onSuccess((responseContent) -> {
                    blocker1.assertEquals(responseContent, content).end();
                })
                .onFailure((errMsg, cause) -> {
                })
                .messageType(messageType)
                .future()
                .onSuccess((outputValue) -> {
                    blocker2.assertEquals(outputValue, content).end();
                })
                .onFailure((errMsg, cause) -> {
                });

        blocker1.awaitEnd();
        blocker2.awaitEnd();
    }

    @Test
    public void testB2CPipe() {
        BusinessChannel businessChannel = APEF.createBusinessChannel(new ChannelConfig().setName("business1"))
                .start();
        ClientChannel clientChannel = APEF.createClientChannel(new ChannelConfig().setName("client"));
        B2CPipe<?, ?> pipe = clientChannel.B2CPipe(businessChannel);

        super.testCommunication(pipe);
    }

    @Test
    public void testB2CPipeTimeout() {
        BusinessChannel businessChannel1 = APEF.createBusinessChannel(new ChannelConfig().setName("business2"));
        businessChannel1.start();
        ClientChannel clientChannel = APEF.createClientChannel(new ChannelConfig().setName("client"));

        B2CPipe<?, ?> b2CPipe = clientChannel.B2CPipe(businessChannel1);

        ////Timeout handler thread is the business channel thread
        testPipeTimeout(clientChannel, b2CPipe, false);
        businessChannel1.close();
        clientChannel.close();
    }

    @Test
    public void testB2CPipeRetry() {
        BusinessChannel businessChannel1 = APEF.createBusinessChannel(new ChannelConfig().setName("business3"));
        businessChannel1.start();
        MessageType messageType = MessageType.newType((byte) 0);
        MessageType messageType2 = MessageType.newType((byte) 1);
        ClientChannel clientChannel = APEF.createClientChannel(new ChannelConfig().setName("client"));
        AtomicInteger exeCount = new AtomicInteger();
        AtomicInteger exeCount2 = new AtomicInteger();
        Blocker blocker = new Blocker();
        clientChannel
                .handler(messageType, (messageContext, requestContent) -> {
                    messageContext.fail("", new Exception());
                    blocker.endIf(exeCount.incrementAndGet() == 6);
                })
                .handler(messageType2, (messageContext, requestContent) -> {
                    exeCount2.incrementAndGet();
                    messageContext.succeed(requestContent);
                }).start();

        B2CPipe<?, ?> pipe = clientChannel.B2CPipe(businessChannel1);

        AtomicInteger failureCount = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        pipe.request()
                .messageType(messageType2)
                .retry(5, 20)
                .onFailure((errMsg, cause) -> {
                    failureCount.incrementAndGet();
                })
                .onSuccess((responseContent) -> {
                    successCount.incrementAndGet();
                })
                .end();
        pipe.request()
                .messageType(messageType)
                .retry(5, 20)
                .onFailure((errMsg, cause) -> {
                    failureCount.incrementAndGet();
                })
                .onSuccess((responseContent) -> {
                    successCount.incrementAndGet();
                })
                .end();

        blocker.awaitEnd();
        //Including retry 2 times, total exe times=3
        assertEquals(exeCount.get(), 6);
        assertEquals(exeCount2.get(), 1);
        assertEquals(failureCount.get(), 1);
        assertEquals(successCount.get(), 1);
        businessChannel1.close();
        clientChannel.close();
    }

    @Test
    public void testB2CPipeIdem() {
        BusinessChannel businessChannel1 = APEF.createBusinessChannel(new ChannelConfig().setName("business4"));
        businessChannel1.start();
        MessageType messageType = MessageType.newType();
        ClientChannel clientChannel = APEF.createClientChannel(new ChannelConfig().setName("client"));
        AtomicInteger exeCount = new AtomicInteger();
        Blocker blocker = new Blocker();
        clientChannel
                .handler(messageType, (messageContext, requestContent) -> {
                    clientChannel.schedule(() -> {
                        exeCount.incrementAndGet();
                        messageContext.succeed(requestContent);
                    }, 300, TimeUnit.MILLISECONDS);
                })
                .start();

        B2CPipe<?, ?> pipe = clientChannel.B2CPipe(businessChannel1);
        AtomicInteger resultCount = new AtomicInteger();
        for (int i = 0; i < 10; i++) {
            pipe.request()
                    .messageType(messageType)
                    .requestContent(i)
                    .idem(1)
                    .onSuccess((responseContent) -> {
                        blocker.endIf(resultCount.incrementAndGet() == 10);
                    })
                    .onFailure((errMsg, cause) -> {
                    })
                    .end();
        }

        blocker.awaitEnd();
        assertEquals(exeCount.get(), 1);
        businessChannel1.close();
        clientChannel.close();
    }

    @Test
    public void testB2CNoReplyIdem() {
        BusinessChannel businessChannel1 = APEF.createBusinessChannel(new ChannelConfig().setName("business1"));
        businessChannel1.start();
        MessageType messageType = MessageType.newType();
        ClientChannel clientChannel = APEF.createClientChannel(new ChannelConfig().setName("client"));
        AtomicInteger exeCount = new AtomicInteger();
        Blocker blocker = new Blocker();
        clientChannel
                .handler(messageType, (messageContext, requestContent) -> {
                    exeCount.incrementAndGet();
                    messageContext.succeed(requestContent);
                })
                .start();

        B2CNoReplyPipe<?> pipe = clientChannel.B2CNoReplyPipe();

        for (int i = 0; i < 10; i++) {
            pipe.noReply()
                    .messageType(messageType)
                    .requestContent(i)
                    .idem(1)
                    .end();
        }

        blocker.awaitAndEnd(200);
        assertEquals(exeCount.get(), 1);
        businessChannel1.close();
        clientChannel.close();
    }
}
