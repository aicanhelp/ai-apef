package io.apef.core.channel;

import io.apef.core.channel.feature.timeout.TimeoutException;
import io.apef.core.channel.message.ChannelMessage;
import io.apef.core.channel.pipe.ChannelNoReplyPipe;
import io.apef.core.channel.pipe.ChannelTxPipe;
import io.apef.testing.unit.BaseUnitSpec;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.concurrent.atomic.AtomicLong;

import static org.testng.Assert.assertEquals;

public abstract class ChannelTest extends BaseUnitSpec {

    protected void testNoReply(ChannelNoReplyPipe channelPipe) {
        Destination destination = new Destination(channelPipe.destChannel());
        for (int i = destination.type1From; i < destination.type1To + 1; i++) {
            channelPipe.noReply()
                    .messageType(TestMessageType.Type1).requestContent(i).end();
        }

        for (int i = destination.type2From; i < destination.type2To + 1; i++) {
            channelPipe.noReply()
                    .messageType(TestMessageType.Type2).requestContent(i).end();
        }
        destination.close();
    }

    /**
     * @param channel    destination channel
     * @param pipe       channelPipe
     * @param sameThread
     */
    protected void testPipeTimeout(Channel channel, ChannelTxPipe pipe, boolean sameThread) {
        AtomicLong threadId = new AtomicLong(-1);
        MessageType messageType = MessageType.newType();
        channel
                .handler(messageType, (messageContext, requestContent) -> {
                    threadId.set(Thread.currentThread().getId());
                })
                .start();
        Blocker blocker = new Blocker();
        AtomicLong threadId2 = new AtomicLong(-1);

        pipe.request()
                .onSuccess((responseContent) -> {
                    blocker.failAndEnd("Not Expect Success.");
                })
                .onFailure((errMsg, cause) -> {
                    threadId2.set(Thread.currentThread().getId());
                    blocker.assertTrue(cause instanceof TimeoutException);
                    blocker.end();
                })
                .timeout(100)
                .messageType(messageType)
                .end();

        blocker.awaitEnd();
        assertEquals(sameThread, threadId.get() == threadId2.get());
        channel.close();
    }

    protected void testCommunication(ChannelTxPipe channelPipe) {
        Destination destination = new Destination(channelPipe.destChannel());
        Source source = new Source(channelPipe, destination);
        source.startTest();
        source.close();
    }

    public static class Source {
        private Channel<?> srcChannel;
        private volatile Blocker blocker1, blocker2;
        ChannelTxPipe channelPipe;
        Destination destination;

        public Source(ChannelTxPipe channelPipe, Destination destination) {
            this.srcChannel = channelPipe.srcChannel();
            this.channelPipe = channelPipe;
            this.destination = destination;
            blocker1 = new Blocker();
            blocker2 = new Blocker();
            this.srcChannel.start();
        }

        public void startTest() {
            for (int i = destination.type1From; i < (destination.type1To + 1); i++) {
                channelPipe.request()
                        .onFailure((errMsg, cause) -> {
                            log.error("", cause);
                            blocker1.endIf(errMsg.endsWith("" + destination.type1To)).fail(errMsg + ":" + cause);
                        })
                        .onSuccess((responseContent) -> {
                            blocker1.endIf((Integer) responseContent == destination.type1To);
                        })
                        .messageType(TestMessageType.Type1)
                        .requestContent(i).end();
            }

            for (int i = destination.type2From; i < (destination.type2To + 1); i++) {
                channelPipe.request()
                        .onFailure((errMsg, cause) -> {
                            log.error("", cause);
                            blocker2.endIf(errMsg.endsWith("" + destination.type2To)).fail(errMsg + ":" + cause);
                        })
                        .onSuccess((responseContent) -> {
                            blocker2.endIf((Integer) responseContent == destination.type2To);
                        })
                        .messageType(TestMessageType.Type2).requestContent(i).end();
            }
        }

        public void close() {
            blocker1.awaitEnd();
            blocker2.awaitEnd();
            srcChannel.close();
            this.destination.close();
        }
    }

    @Getter
    @Accessors(fluent = true)
    public static class Destination {

        private Channel<?> destChannel;
        private volatile Blocker blocker1, blocker2;
        int type1From = 0, type1To = 20;
        int type2From = 21, type2To = 30;

        protected Destination(Channel<?> destChannel) {
            this.destChannel = destChannel;
            blocker1 = new Blocker();
            blocker2 = new Blocker();

            destChannel.<Integer, Integer, ChannelMessage<Integer, Integer>>handler(TestMessageType.Type1, requestMessage -> {
                if (requestMessage.requestContent() < (type1To + 1))
                    requestMessage.messageContext().succeed(requestMessage.requestContent());
                else
                    requestMessage.messageContext().fail("No Type1 message:" + requestMessage.requestContent(), null);
                blocker1.assertTrue(requestMessage.requestContent() < (type1To + 1))
                        .endIf(requestMessage.requestContent() == type1To);
            }).<Integer, Integer, ChannelMessage<Integer, Integer>>handler(TestMessageType.Type2, requestMessage -> {
                if (requestMessage.requestContent() > (type2From - 1))
                    requestMessage.messageContext().succeed(requestMessage.requestContent());
                else
                    requestMessage.messageContext().fail("No Type2 message:" + requestMessage.requestContent(), null);
                blocker2.assertTrue(requestMessage.requestContent() > (type2From - 1))
                        .endIf(requestMessage.requestContent() == type2To);
            });
            destChannel.start();
        }

        public void close() {
            blocker1.awaitEnd();
            blocker2.awaitEnd();

            destChannel.close();
        }
    }
}
