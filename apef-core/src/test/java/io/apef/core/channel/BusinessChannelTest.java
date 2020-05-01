package io.apef.core.channel;

import io.apef.core.APEF;
import io.apef.core.channel.future.ChannelFuture;
import io.apef.core.channel.pipe.*;
import io.apef.core.utils.scheduler.Scheduler;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public class BusinessChannelTest extends ChannelTest {
    @Test
    public void testB2BNoReply() {
        BusinessChannel businessChannel = APEF.createBusinessChannel(new ChannelConfig().setName("business1"));
        B2BNoReplyPipe<?> b2BNoReplyPipe = businessChannel.B2BNoReplyPipe();

        super.testNoReply(b2BNoReplyPipe);
    }

    @Test
    public void testS2BNoReply() {
        BusinessChannel businessChannel = APEF.createBusinessChannel(new ChannelConfig().setName("business1"));
        S2BNoReplyPipe<?> s2BNoReplyPipe = businessChannel.S2BNoReplyPipe();

        super.testNoReply(s2BNoReplyPipe);
    }

    @Test
    public void testB2BPipe() {
        BusinessChannel businessChannel1 =
                APEF.createBusinessChannel(new ChannelConfig().setName("business1")).start();
        BusinessChannel businessChannel2 = APEF.createBusinessChannel(new ChannelConfig().setName("business2"));
        B2BPipe<?, ?> b2BPipe = businessChannel2.B2BPipe(businessChannel1);

        super.testCommunication(b2BPipe);
    }

    @Test
    public void testS2BPipe() {
        ServerChannel serverChannel = APEF.createServerChannel(new ChannelConfig().setName("server"))
                .start();
        BusinessChannel businessChannel2 = APEF.createBusinessChannel(new ChannelConfig().setName("business2"));
        S2BPipe<?, ?> s2BPipe = businessChannel2.S2BPipe(serverChannel);

        super.testCommunication(s2BPipe);
    }

    @Test
    public void testThreadsOfB2BPipe() {
        BusinessChannel businessChannel1 = APEF.createBusinessChannel(new ChannelConfig().setName("business1"));
        businessChannel1.start();
        BusinessChannel businessChannel2 = APEF.createBusinessChannel(new ChannelConfig().setName("business2"));
        testThreadsOfPipe(businessChannel2, businessChannel2.B2BPipe(businessChannel1), false);
        businessChannel1.close();
        businessChannel2.close();
    }

    @Test
    public void testThreadsOfS2BPipe() {
        ServerChannel serverChannel = APEF.createServerChannel(new ChannelConfig().setName("server"));
        serverChannel.start();
        BusinessChannel businessChannel2 = APEF.createBusinessChannel(new ChannelConfig().setName("business2"));
        testThreadsOfPipe(businessChannel2, businessChannel2.S2BPipe(serverChannel), true);
        serverChannel.close();
        businessChannel2.close();
    }

    private void testThreadsOfPipe(BusinessChannel businessChannel, ChannelTxPipe pipe, boolean same) {
        MessageType testType = MessageType.newType();
        AtomicLong threadId = new AtomicLong(-1);
        AtomicInteger counter = new AtomicInteger();
        int testCount = 100000;
        Blocker blocker = new Blocker();
        businessChannel.handler(testType, (messageContext, requestContent) -> {
            long currentId = Thread.currentThread().getId();
            long oldId = threadId.getAndSet(currentId);
            if (oldId != -1) {
                blocker.assertTrue(currentId == oldId);
            }
            blocker.endIf(counter.incrementAndGet() == testCount);
            messageContext.succeed(requestContent);
        }).start();

        AtomicLong threadId2 = new AtomicLong(-1);
        Blocker blocker2 = new Blocker();
        for (int i = 0; i < testCount; i++) {
            pipe.request()
                    .onFailure((errMsg, cause) -> {
                    })
                    .onSuccess((responseContent) -> {
                        long currentId = Thread.currentThread().getId();
                        long oldId = threadId2.getAndSet(currentId);
                        if (oldId != -1) {
                            blocker.assertTrue(currentId == oldId);
                        }
                        blocker2.endIf((int) responseContent == (testCount - 1));
                    })
                    .messageType(testType)
                    .requestContent(i)
                    .end();
        }
        assertEquals(same, threadId.get() == threadId2.get());
        blocker.awaitEnd();
        blocker2.awaitEnd();
    }

    @Test
    public void testB2BPipeTimeout() {
        BusinessChannel businessChannel1 = APEF.createBusinessChannel(new ChannelConfig().setName("business1"));
        businessChannel1.start();
        BusinessChannel businessChannel2 = APEF.createBusinessChannel(new ChannelConfig().setName("business2"));

        B2BPipe<?, ?> b2BPipe = businessChannel2.B2BPipe(businessChannel1);

        //Timeout handler thread is the source business channel thread
        testPipeTimeout(businessChannel2, b2BPipe, false);
        businessChannel1.close();
        businessChannel2.close();
    }

    @Test
    public void testB2BPipeIdem() {
        BusinessChannel businessChannel1 = APEF.createBusinessChannel(new ChannelConfig().setName("business1"));
        businessChannel1.start();
        MessageType messageType = MessageType.newType((byte) 0);
        BusinessChannel businessChannel2 = APEF.createBusinessChannel(new ChannelConfig().setName("business2"));
        AtomicInteger exeCount = new AtomicInteger();
        Blocker blocker = new Blocker();
        businessChannel2
                .handler(messageType, (messageContext, requestContent) -> {
                    businessChannel2.schedule(() -> {
                        exeCount.incrementAndGet();
                        messageContext.succeed(requestContent);
                    }, 300, TimeUnit.MILLISECONDS);
                })
                .start();

        B2BPipe<?, ?> pipe = businessChannel2.B2BPipe(businessChannel1);
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
        businessChannel2.close();
    }

    @Test
    public void testB2BNoReplyIdem() {
        BusinessChannel businessChannel1 = APEF.createBusinessChannel(new ChannelConfig().setName("business1"));
        businessChannel1.start();
        MessageType messageType = MessageType.newType((byte) 0);
        BusinessChannel businessChannel2 = APEF.createBusinessChannel(new ChannelConfig().setName("business2"));
        AtomicInteger exeCount = new AtomicInteger();
        Blocker blocker = new Blocker();
        businessChannel2
                .handler(messageType, (messageContext, requestContent) -> {
                    exeCount.incrementAndGet();
                    messageContext.succeed(requestContent);
                })
                .start();

        B2BNoReplyPipe<?> pipe = businessChannel2.B2BNoReplyPipe();
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
        businessChannel2.close();
    }

    @Test
    public void testS2BPipeTimeout() {
        ServerChannel serverChannel = APEF.createServerChannel(new ChannelConfig().setName("server"));
        serverChannel.start();
        BusinessChannel businessChannel2 = APEF.createBusinessChannel(new ChannelConfig().setName("business2"));

        S2BPipe<?, ?> pipe = businessChannel2.S2BPipe(serverChannel);

        //Timeout handler thread is same with the business channel thread
        testPipeTimeout(businessChannel2, pipe, true);
        serverChannel.close();
        businessChannel2.close();
    }

    @Test
    public void testS2BPipeIdem() {
        ServerChannel serverChannel = APEF.createServerChannel(new ChannelConfig().setName("server"));
        serverChannel.start();
        MessageType messageType = MessageType.newType();
        BusinessChannel businessChannel2 = APEF.createBusinessChannel(new ChannelConfig().setName("business2"));
        AtomicInteger exeCount = new AtomicInteger();
        Blocker blocker = new Blocker();
        //todo: need to refactor the channel scheduler
        businessChannel2
                .handler(messageType, (messageContext, requestContent) -> {
                    exeCount.incrementAndGet();
                    Scheduler.schedule()
                            .delay(200)
                            .scheduleTask(scheduleContext -> {
                                messageContext.succeed(requestContent);
                            })
                            .start();
                })
                .start();

        S2BPipe<?, ?> pipe = businessChannel2.S2BPipe(serverChannel);
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
        serverChannel.close();
        businessChannel2.close();
    }

    @Test
    public void testS2BNoReplyIdem() {
        ServerChannel serverChannel = APEF.createServerChannel(new ChannelConfig().setName("server"));
        serverChannel.start();
        MessageType messageType = MessageType.newType();
        BusinessChannel businessChannel2 = APEF.createBusinessChannel(new ChannelConfig().setName("business2"));
        AtomicInteger exeCount = new AtomicInteger();
        Blocker blocker = new Blocker();
        businessChannel2
                .handler(messageType, (messageContext, requestContent) -> {
                    exeCount.incrementAndGet();
                    Scheduler.schedule()
                            .delay(20)
                            .scheduleTask(scheduleContext -> {
                                messageContext.succeed(requestContent);
                            })
                            .start();
                })
                .start();

        B2BNoReplyPipe<?> pipe = businessChannel2.B2BNoReplyPipe();

        for (int i = 0; i < 10; i++) {
            pipe.noReply()
                    .messageType(messageType)
                    .requestContent(i)
                    .idem(1)
                    .end();
        }
        blocker.awaitAndEnd(200);
        assertEquals(exeCount.get(), 1);
        serverChannel.close();
        businessChannel2.close();
    }

    @Test
    public void testExecuteOnChannelThread() {
        BusinessChannel businessChannel = APEF.createBusinessChannel(new ChannelConfig().setName("test"));
        businessChannel.start();
        Set<Long> threadsId = new HashSet<>();

        AtomicInteger counter = new AtomicInteger();
        Blocker blocker = new Blocker();
        for (int i = 0; i < 1000; i++) {
            businessChannel.execute(() -> {
                try {
                    threadsId.add(Thread.currentThread().getId());
                } catch (Exception ex) {

                }
                if (counter.incrementAndGet() == 1000) blocker.end();
            });
        }
        blocker.awaitEnd();
        assertEquals(threadsId.size(), 1);
        assertNotEquals(threadsId.iterator().next(), Thread.currentThread().getId());

        businessChannel.close();
    }

    @Test
    public void testChannelSchedule() {
        BusinessChannel businessChannel = APEF.createBusinessChannel(new ChannelConfig().setName("test"));
        businessChannel.start();

        Blocker blocker = new Blocker();
        businessChannel.schedule(() -> {
            blocker.endOut(100);
        }, 110, TimeUnit.MILLISECONDS);

        blocker.awaitEnd();
    }

    @Test
    public void testChannelRetry() {
        BusinessChannel<?> businessChannel = APEF.createBusinessChannel(new ChannelConfig().setName("test"));
        businessChannel.start();

        Blocker blocker = new Blocker();

        AtomicInteger inputContext = new AtomicInteger();
        businessChannel.schedule(inputContext)
                .interval(100)
                .times(3)
                .timeUnit(TimeUnit.MILLISECONDS)
                .completeOnSuccess(true)
                .submit(input -> {
                    input.incrementAndGet();
                    if (input.get() < 2) {
                        return ChannelFuture.completeFuture(false);
                    } else {
                        return ChannelFuture.completeFuture(true);
                    }
                })
                .onSuccess((outputValue) -> {
                    blocker.assertEquals(outputValue.get(), 2).end();
                })
                .onFailure((errMsg, cause) -> {
                    blocker.failAndEnd("should be success");
                });

        blocker.awaitEnd();
        businessChannel.close();
    }

    @Test
    public void testCountDownLatch() {
        BusinessChannel<?> businessChannel = APEF.createBusinessChannel(new ChannelConfig().setName("test"));
        businessChannel.start();

        Blocker blocker = new Blocker();

        AtomicInteger inputContext = new AtomicInteger();
        businessChannel.batch(inputContext)
                .concurrency(1)
                .timeout(1)
                .timeUnit(TimeUnit.SECONDS)
                .task("task1")
                .submit(input -> {
                    input.incrementAndGet();
                    return ChannelFuture.completeFuture(true);
                })
                .task("task2")
                .submit(input -> {
                    input.incrementAndGet();
                    return ChannelFuture.completeFuture(true);
                })
                .start()
                .onSuccess((outputValue) -> {
                    blocker.assertEquals(outputValue.get(), 2).end();
                })
                .onFailure((errMsg, cause) -> {
                    blocker.failAndEnd("should be success");
                });

        blocker.awaitEnd();
        businessChannel.close();
    }
}
