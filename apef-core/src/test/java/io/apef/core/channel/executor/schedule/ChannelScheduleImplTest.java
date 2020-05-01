package io.apef.core.channel.executor.schedule;

import io.apef.core.APEF;
import io.apef.core.channel.BusinessChannel;
import io.apef.core.channel.ChannelConfig;
import io.apef.core.channel.ClientChannel;
import io.apef.core.channel.MessageType;
import io.apef.core.channel.executor.ChannelScheduleCancelException;
import io.apef.core.channel.feature.timeout.TimeoutException;
import io.apef.core.channel.future.ChannelFuture;
import io.apef.core.channel.message.ChannelMessageContext;
import io.apef.core.channel.pipe.B2BPipe;
import io.apef.core.channel.pipe.B2CPipe;
import io.apef.core.utils.ChannelTimer;
import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Test
public class ChannelScheduleImplTest extends BaseUnitSpec {
    @Override
    protected void doBeforeClass() {
        ChannelTimer.timer();
    }

    public void testToVerifyIsSingleThreadExecuteImmediately1() {
        BusinessChannel<?> businessChannel =
                APEF.createBusinessChannel(new ChannelConfig().setName("testToVerifyIsSingleThreadExecuteImmediately1"))
                        .start();
        ChannelSchedule<String> channelSchedule = new ChannelScheduleImpl<>(businessChannel);
        Blocker blocker = new Blocker();
        businessChannel.execute(() -> {
            long threadId = Thread.currentThread().getId();
            channelSchedule.withOutputContext("1")
                    .times(1)
                    .submit(inputContext -> ChannelFuture.completeFuture(true))
                    .onSuccess((outputValue) -> {
                        blocker.assertEquals(outputValue, "1")
                                .assertEquals(Thread.currentThread().getId(), threadId)
                                .endIn(100);
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker.failAndEnd("Should be success");
                    });
        });

        blocker.awaitEnd();
        businessChannel.close();
    }

    public void testToVerifyIsSingleThreadExecuteImmediately2() {
        MessageType messageType = MessageType.newType();
        BusinessChannel<?> businessChannel1 =
                APEF.createBusinessChannel(new ChannelConfig().setName("testExecuteImmediately2_1"))
                        .start();
        BusinessChannel<?> businessChannel2 =
                APEF.createBusinessChannel(new ChannelConfig().setName("testExecuteImmediately2_2"))
                        .handler(messageType, ChannelMessageContext::succeed)
                        .start();
        ChannelSchedule<String> channelSchedule = new ChannelScheduleImpl<>(businessChannel1);
        B2BPipe<?, ?> pipe = businessChannel2.B2BPipe(businessChannel1);
        Blocker blocker = new Blocker();
        businessChannel1.execute(() -> {
            long threadId = Thread.currentThread().getId();
            channelSchedule.withOutputContext("1")
                    .times(1)
                    .submit(inputContext ->
                            pipe.request().messageType(messageType).onSuccess((outputValue) -> {
                            }).onFailure((errMsg, cause) -> {
                            }).future()
                    )
                    .onSuccess((outputValue) -> {
                        blocker.assertEquals(outputValue, "1")
                                .assertEquals(Thread.currentThread().getId(), threadId)
                                .endIn(100);
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker.failAndEnd("Should be success");
                    });
        });

        blocker.awaitEnd();
        businessChannel1.close();
        businessChannel2.close();
    }

    public void testExecuteDelay1() {
        BusinessChannel<?> businessChannel =
                APEF.createBusinessChannel(new ChannelConfig().setName("testExecuteDelay1"))
                        .start();
        ChannelSchedule<String> channelSchedule = new ChannelScheduleImpl<>(businessChannel);
        Blocker blocker = new Blocker();
        businessChannel.execute(() -> {
            channelSchedule.withOutputContext("1")
                    .times(1)
                    .delay(100)
                    .timeUnit(TimeUnit.MILLISECONDS)
                    .submit(inputContext -> ChannelFuture.completeFuture(true))
                    .onSuccess((outputValue) -> {
                        blocker.assertEquals(outputValue, "1")
                                .endOut(50);
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker.failAndEnd("Should be success");
                    });
        });

        blocker.awaitEnd();
        businessChannel.close();
    }

    public void testExecuteDelay2() {
        MessageType messageType = MessageType.newType();
        BusinessChannel<?> businessChannel1 =
                APEF.createBusinessChannel(new ChannelConfig().setName("testExecuteImmediately2_1"))
                        .start();
        BusinessChannel<?> businessChannel2 =
                APEF.createBusinessChannel(new ChannelConfig().setName("testExecuteImmediately2_2"))
                        .handler(messageType, ChannelMessageContext::succeed)
                        .start();
        ChannelSchedule<String> channelSchedule = new ChannelScheduleImpl<>(businessChannel1);
        B2BPipe<?, ?> pipe = businessChannel2.B2BPipe(businessChannel1);
        Blocker blocker = new Blocker();
        businessChannel1.execute(() -> {
            channelSchedule.withOutputContext("1")
                    .times(1)
                    .delay(100)
                    .timeUnit(TimeUnit.MILLISECONDS)
                    .submit(inputContext ->
                            pipe.request().messageType(messageType).onSuccess((outputValue) -> {
                            }).onFailure((errMsg, cause) -> {
                            }).future()
                    )
                    .onSuccess((outputValue) -> {
                        blocker.assertEquals(outputValue, "1")
                                .endOut(50);
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker.failAndEnd("Should be success");
                    });
        });

        blocker.awaitEnd();
        businessChannel1.close();
        businessChannel2.close();
    }

    public void testDelayIntervalTimes1() {
        BusinessChannel<?> businessChannel =
                APEF.createBusinessChannel(new ChannelConfig().setName("testDelayIntervalTimes1"))
                        .start();
        ChannelSchedule<AtomicInteger> channelSchedule = new ChannelScheduleImpl<>(businessChannel);

        Blocker blocker = new Blocker();
        businessChannel.execute(() -> {
            AtomicInteger input = new AtomicInteger();
            channelSchedule.withOutputContext(input)
                    .delay(100)
                    .interval(10)
                    .timeUnit(TimeUnit.MILLISECONDS)
                    .times(4)
                    .submit(inputContext -> {
                        inputContext.incrementAndGet();
                        return ChannelFuture.completeFuture(true);
                    })
                    .onSuccess((outputValue) -> {
                        blocker.assertEquals(outputValue.get(), 4)
                                .endIn(100, 400);
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker.failAndEnd("Should be success");
                    });
        });

        blocker.awaitEnd();
        businessChannel.close();

    }

    public void testDelayIntervalTimes2() {
        MessageType messageType = MessageType.newType();
        BusinessChannel<?> businessChannel1 =
                APEF.createBusinessChannel(new ChannelConfig().setName("testTimes2_1"))
                        .start();
        BusinessChannel<?> businessChannel2 =
                APEF.createBusinessChannel(new ChannelConfig().setName("testTimes2_2"))
                        .handler(messageType, ChannelMessageContext::succeed)
                        .start();
        ChannelSchedule<AtomicInteger> channelSchedule = new ChannelScheduleImpl<>(businessChannel1);
        B2BPipe<?, ?> pipe = businessChannel2.B2BPipe(businessChannel1);
        Blocker blocker = new Blocker();
        businessChannel1.execute(() -> {
            AtomicInteger input = new AtomicInteger();
            channelSchedule.withOutputContext(input)
                    .delay(100)
                    .interval(10)
                    .timeUnit(TimeUnit.MILLISECONDS)
                    .times(4)
                    .submit(inputContext ->
                            pipe.request().messageType(messageType).onSuccess((outputValue) -> {
                                inputContext.incrementAndGet();
                            }).onFailure((errMsg, cause) -> {
                            }).future()
                    )
                    .onSuccess((outputValue) -> {
                        blocker.assertEquals(outputValue.get(), 4)
                                .endIn(100, 400);
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker.failAndEnd("Should be success");
                    });
        });

        blocker.awaitEnd();
        businessChannel1.close();
        businessChannel2.close();
    }

    public void testStopOnFailure() {
        BusinessChannel<?> businessChannel =
                APEF.createBusinessChannel(new ChannelConfig().setName("testUntilFailure1"))
                        .start();
        ChannelSchedule<AtomicInteger> channelSchedule = new ChannelScheduleImpl<>(businessChannel);

        Blocker blocker = new Blocker();
        businessChannel.execute(() -> {
            AtomicInteger input = new AtomicInteger();
            channelSchedule.withOutputContext(input)
                    .timeUnit(TimeUnit.MILLISECONDS)
                    .times(4)
                    .stopOnFailure(true)
                    .submit(inputContext -> {
                        inputContext.incrementAndGet();
                        if (inputContext.get() == 3)
                            return ChannelFuture.completeFuture(false);
                        return ChannelFuture.completeFuture(true);
                    })
                    .onSuccess((outputValue) -> {
                        blocker.failAndEnd("Should be failure");
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker.assertEquals(input.get(), 3)
                                .end();
                    });
        });

        blocker.awaitEnd();
        businessChannel.close();
    }

    public void testRetry() {
        BusinessChannel<?> businessChannel =
                APEF.createBusinessChannel(new ChannelConfig().setName("testRetry"))
                        .start();
        ChannelSchedule<AtomicInteger> channelSchedule = new ChannelScheduleImpl<>(businessChannel);

        Blocker blocker = new Blocker();
        businessChannel.execute(() -> {
            AtomicInteger input = new AtomicInteger();
            channelSchedule.withOutputContext(input)
                    .timeUnit(TimeUnit.MILLISECONDS)
                    .times(4)
                    .completeOnSuccess(true)
                    .submit(inputContext -> {
                        inputContext.incrementAndGet();
                        if (inputContext.get() == 4)
                            return ChannelFuture.completeFuture(true);
                        return ChannelFuture.completeFuture(false);
                    })
                    .onSuccess((outputValue) -> {
                        blocker.assertEquals(outputValue.get(), 4)
                                .end();
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker.failAndEnd("Should be success");
                    });
        });

        blocker.awaitEnd();
        businessChannel.close();
    }

    public void testCompleteOn() {
        BusinessChannel<?> businessChannel =
                APEF.createBusinessChannel(new ChannelConfig().setName("testCompleteOn"))
                        .start();
        ChannelSchedule<AtomicInteger> channelSchedule = new ChannelScheduleImpl<>(businessChannel);

        Blocker blocker = new Blocker();
        businessChannel.execute(() -> {
            AtomicInteger input = new AtomicInteger();
            channelSchedule.withOutputContext(input)
                    .times(100)
                    .completeOn(outputContext -> input.get() == 3)
                    .submit(inputContext -> {
                        inputContext.incrementAndGet();
                        return ChannelFuture.completeFuture(true);
                    })
                    .onSuccess((outputValue) -> {
                        blocker.assertEquals(outputValue.get(), 3)
                                .end();
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker.failAndEnd("Should be success");
                    });
        });

        blocker.awaitEnd();
        businessChannel.close();
    }

    public void testCompleteOn2() {
        BusinessChannel<?> businessChannel =
                APEF.createBusinessChannel(new ChannelConfig().setName("testCompleteOn"))
                        .start();
        ChannelSchedule<AtomicInteger> channelSchedule = new ChannelScheduleImpl<>(businessChannel);

        Blocker blocker = new Blocker();
        businessChannel.execute(() -> {
            AtomicInteger input = new AtomicInteger();
            channelSchedule.withOutputContext(input)
                    .times(3)
                    .completeOn(outputContext -> input.get() == 10)
                    .submit(inputContext -> {
                        inputContext.incrementAndGet();
                        return ChannelFuture.completeFuture(true);
                    })
                    .onSuccess((outputValue) -> {
                        blocker.assertEquals(outputValue.get(), 3)
                                .end();
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker.failAndEnd("Should be success");
                    });
        });

        blocker.awaitEnd();
        businessChannel.close();
    }

    public void testTimeout() {
        MessageType messageType = MessageType.newType();
        BusinessChannel<?> businessChannel1 =
                APEF.createBusinessChannel(new ChannelConfig().setName("testTimeout_1"))
                        .start();
        BusinessChannel<?> businessChannel2 =
                APEF.createBusinessChannel(new ChannelConfig().setName("testTimeout_2"))
                        .handler(messageType, (messageContext, requestContent) -> {
                            blockingMs(100);
                            messageContext.succeed(requestContent);
                        })
                        .start();
        ChannelSchedule<AtomicInteger> channelSchedule = new ChannelScheduleImpl<>(businessChannel1);
        B2BPipe<?, ?> pipe = businessChannel2.B2BPipe(businessChannel1);
        Blocker blocker = new Blocker();
        businessChannel1.execute(() -> {
            AtomicInteger input = new AtomicInteger();
            channelSchedule.withOutputContext(input)
                    .timeout(10)
                    .timeUnit(TimeUnit.MILLISECONDS)
                    .submit(inputContext ->
                            pipe.request().messageType(messageType).onSuccess((outputValue) -> {
                                inputContext.incrementAndGet();
                            }).onFailure((errMsg, cause) -> {
                            }).future()
                    )
                    .onSuccess((outputValue) -> {
                        blocker.failAndEnd("Should be success");
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker.assertTrue(cause instanceof TimeoutException)
                                .end();
                    });
        });

        blocker.awaitEnd();
        businessChannel1.close();
        businessChannel2.close();
    }


    public void testScheduleFuture() {
        MessageType messageType = MessageType.newType();
        BusinessChannel<?> businessChannel1 =
                APEF.createBusinessChannel(new ChannelConfig().setName("testScheduleFuture2_1"))
                        .start();
        BusinessChannel<?> businessChannel2 =
                APEF.createBusinessChannel(new ChannelConfig().setName("testScheduleFuture2_2"))
                        .handler(messageType, ChannelMessageContext::succeed)
                        .start();
        ChannelSchedule<AtomicInteger> channelSchedule = new ChannelScheduleImpl<>(businessChannel1);
        B2BPipe<?, ?> pipe = businessChannel2.B2BPipe(businessChannel1);
        Blocker blocker = new Blocker();
        businessChannel1.execute(() -> {
            AtomicInteger input = new AtomicInteger();
            ChannelScheduleFuture<AtomicInteger> scheduleFuture = channelSchedule.withOutputContext(input)
                    .times(3)
                    .submit(inputContext ->
                            pipe.request().messageType(messageType).onSuccess((outputValue) -> {
                                inputContext.incrementAndGet();
                            }).onFailure((errMsg, cause) -> {
                            }).future()
                    );

            AtomicInteger counter = new AtomicInteger();
            for (int i = 0; i < 10; i++) {
                scheduleFuture
                        .onSuccess((outputValue) -> {
                            counter.incrementAndGet();
                            blocker.assertEquals(outputValue.get(), 3);
                            if (counter.get() == 10)
                                blocker.end();
                        })
                        .onFailure((errMsg, cause) -> {
                            blocker.failAndEnd("Should be success");
                        });
            }
        });

        blocker.awaitEnd();
        businessChannel1.close();
        businessChannel2.close();
    }

    public void testCancelScheduleFuture() {
        MessageType messageType = MessageType.newType();
        BusinessChannel<?> businessChannel1 =
                APEF.createBusinessChannel(new ChannelConfig().setName("testCancelScheduleFuture_1"))
                        .start();
        BusinessChannel<?> businessChannel2 =
                APEF.createBusinessChannel(new ChannelConfig().setName("testCancelScheduleFuture_2"))
                        .handler(messageType, (messageContext, requestContent) -> {
                            blockingMs(100);
                            messageContext.succeed(requestContent);
                        })
                        .start();
        ChannelSchedule<AtomicInteger> channelSchedule = new ChannelScheduleImpl<>(businessChannel1);
        B2BPipe<?, ?> pipe = businessChannel2.B2BPipe(businessChannel1);
        Blocker blocker = new Blocker();
        businessChannel1.execute(() -> {
            AtomicInteger input = new AtomicInteger();
            ChannelScheduleFuture<AtomicInteger> scheduleFuture = channelSchedule.withOutputContext(input)
                    .times(3)
                    .submit(inputContext ->
                            pipe.request().messageType(messageType).onSuccess((outputValue) -> {
                                inputContext.incrementAndGet();
                            }).onFailure((errMsg, cause) -> {
                            }).future()
                    );

            scheduleFuture.cancel();
            scheduleFuture
                    .onSuccess((outputValue) -> {
                        blocker.failAndEnd("Should be failure");
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker.assertTrue(cause instanceof ChannelScheduleCancelException)
                                .end();
                    });
        });

        blocker.awaitEnd();
        businessChannel1.close();
        businessChannel2.close();
    }

    public void testMultiLevelRetry() {
        MessageType messageType = MessageType.newType();
        BusinessChannel<?> businessChannel =
                APEF.createBusinessChannel(new ChannelConfig().setName("testScheduleFuture2_1"))
                        .start();
        AtomicInteger counter = new AtomicInteger();
        AtomicInteger counter2 = new AtomicInteger();
        ClientChannel<?> clientChannel =
                APEF.createClientChannel(new ChannelConfig().setName("testScheduleFuture2_2"))
                        .handler(messageType, (messageContext, requestContent) -> {
                            counter2.incrementAndGet();
                            if (counter.get() == 2) {
                                messageContext.succeed(requestContent);
                            } else
                                messageContext.fail("EEE", null);
                        })
                        .start();
        ChannelSchedule<AtomicInteger> channelSchedule = new ChannelScheduleImpl<>(businessChannel);
        B2CPipe<?, ?> pipe = clientChannel.B2CPipe(businessChannel);

        Blocker blocker = new Blocker();

        TestSession testSession = new TestSession();
        businessChannel.execute(() -> {
            AtomicInteger input = new AtomicInteger();
            channelSchedule.withOutputContext(input)
                    .timeUnit(TimeUnit.MILLISECONDS)
                    .times(4)
                    .completeOnSuccess(true)
                    .submit(inputContext ->
                            businessChannel.sequential(testSession)
                                    .stopOnFailure(true)
                                    .addTaskToTail(session -> {
                                        counter.incrementAndGet();
                                        testSession.value1 = 9999;
                                        return ChannelFuture.completeFuture(testSession);
                                    })
                                    .addTaskToTail(outputContext -> pipe.request()
                                            .retry(2, 100)
                                            .messageType(messageType)
                                            .requestContent(testSession.value1)
                                            .onSuccess(outputValue -> {
                                                testSession.value2 = "" + outputValue;
                                            })
                                            .future()
                                    ).start())
                    .onSuccess((outputValue) -> {
                        blocker.assertEquals(counter.get(), 2)
                                //run times= one time failures+2 retries failures+one time success=4
                                .assertEquals(counter2.get(), 4)
                                .assertEquals(testSession.value2, "9999")
                                .end();
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker.failAndEnd("Should be success");
                    });
        });

        blocker.awaitEnd();
        businessChannel.close();
        clientChannel.close();
    }

    static class TestSession {
        int value1;
        String value2;
    }
}