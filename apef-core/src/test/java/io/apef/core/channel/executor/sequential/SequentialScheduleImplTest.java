package io.apef.core.channel.executor.sequential;

import io.apef.core.APEF;
import io.apef.core.channel.BusinessChannel;
import io.apef.core.channel.ChannelConfig;
import io.apef.core.channel.MessageType;
import io.apef.core.channel.feature.timeout.TimeoutException;
import io.apef.core.channel.future.ChannelFuture;
import io.apef.core.channel.message.ChannelMessageContext;
import io.apef.core.channel.pipe.B2BPipe;
import io.apef.core.utils.ChannelTimer;
import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Test
public class SequentialScheduleImplTest extends BaseUnitSpec {
    @Override
    protected void doBeforeClass() {
        ChannelTimer.timer();
    }

    public void testOneTask1() {
        BusinessChannel<?> businessChannel =
                APEF.createBusinessChannel(new ChannelConfig().setName("testOneTask1"))
                        .start();
        SequentialSchedule<String> channelSchedule = new SequentialScheduleImpl<>(businessChannel);
        Blocker blocker = new Blocker();
        businessChannel.execute(() -> {
            long threadId = Thread.currentThread().getId();
            channelSchedule.withOutputContext("1")
                    .addTaskToTail(inputContext -> ChannelFuture.completeFuture(true))
                    .start()
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

    public void testOneTask2() {
        MessageType messageType = MessageType.newType();
        BusinessChannel<?> businessChannel1 =
                APEF.createBusinessChannel(new ChannelConfig().setName("testOneTask2_1"))
                        .start();
        BusinessChannel<?> businessChannel2 =
                APEF.createBusinessChannel(new ChannelConfig().setName("testOneTask2_2"))
                        .handler(messageType, ChannelMessageContext::succeed)
                        .start();
        SequentialSchedule<String> channelSchedule = new SequentialScheduleImpl<>(businessChannel1);
        B2BPipe<?, ?> pipe = businessChannel2.B2BPipe(businessChannel1);
        Blocker blocker = new Blocker();
        businessChannel1.execute(() -> {
            long threadId = Thread.currentThread().getId();
            channelSchedule.withOutputContext("1")
                    .addTaskToTail(inputContext ->
                            pipe.request().messageType(messageType).onSuccess((outputValue) -> {
                            }).onFailure((errMsg, cause) -> {
                            }).future()
                    )
                    .start()
                    .onSuccess((outputValue) -> {
                        blocker
                                .assertEquals(outputValue, "1")
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

    public void testToVerifySequential() {
        MessageType messageType = MessageType.newType();
        BusinessChannel<?> businessChannel1 =
                APEF.createBusinessChannel(new ChannelConfig().setName("testTimeout_1"))
                        .start();
        BusinessChannel<?> businessChannel2 =
                APEF.createBusinessChannel(new ChannelConfig().setName("testTimeout_2"))
                        .handler(messageType, (messageContext, requestContent) -> {
                            blockingMs(50);
                            messageContext.succeed(requestContent);
                        })
                        .start();
        SequentialSchedule<AtomicInteger> channelSchedule = new SequentialScheduleImpl<>(businessChannel1);
        B2BPipe<?, ?> pipe = businessChannel2.B2BPipe(businessChannel1);
        Blocker blocker = new Blocker();
        businessChannel1.execute(() -> {
            AtomicInteger input = new AtomicInteger();
            channelSchedule.withOutputContext(input)
                    .addTaskToTail(inputContext ->
                            pipe.request().messageType(messageType).onSuccess((outputValue) -> {
                                inputContext.incrementAndGet();
                            }).onFailure((errMsg, cause) -> {
                            }).future()
                    )
                    .addTaskToTail(inputContext ->
                            pipe.request().messageType(messageType).onSuccess((outputValue) -> {
                                inputContext.incrementAndGet();
                            }).onFailure((errMsg, cause) -> {
                            }).future()
                    )
                    .addTaskToTail(inputContext ->
                            pipe.request().messageType(messageType).onSuccess((outputValue) -> {
                                inputContext.incrementAndGet();
                            }).onFailure((errMsg, cause) -> {
                            }).future()
                    )
                    .start()
                    .onSuccess((outputValue) -> {
                        blocker.endOut(150);
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker.failAndEnd("Should be failure");
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
        SequentialSchedule<AtomicInteger> channelSchedule = new SequentialScheduleImpl<>(businessChannel);

        Blocker blocker = new Blocker();
        businessChannel.execute(() -> {
            AtomicInteger input = new AtomicInteger();
            channelSchedule.withOutputContext(input)
                    .stopOnFailure(true)
                    .addTaskToTail(inputContext -> {
                        inputContext.incrementAndGet();
                        return ChannelFuture.completeFuture(true);
                    })
                    .addTaskToTail(inputContext -> {
                        inputContext.incrementAndGet();
                        return ChannelFuture.completeFuture(false);
                    })
                    .addTaskToTail(inputContext -> {
                        inputContext.incrementAndGet();
                        return ChannelFuture.completeFuture(true);
                    })
                    .start()
                    .onSuccess((outputValue) -> {
                        blocker.failAndEnd("Should be failure");
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker.assertEquals(input.get(), 2)
                                .end();
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
        SequentialSchedule<AtomicInteger> channelSchedule = new SequentialScheduleImpl<>(businessChannel1);
        B2BPipe<?, ?> pipe = businessChannel2.B2BPipe(businessChannel1);
        Blocker blocker = new Blocker();
        businessChannel1.execute(() -> {
            AtomicInteger input = new AtomicInteger();
            channelSchedule.withOutputContext(input)
                    .timeout(10)
                    .timeUnit(TimeUnit.MILLISECONDS)
                    .addTaskToTail(inputContext ->
                            pipe.request().messageType(messageType).onSuccess((outputValue) -> {
                                inputContext.incrementAndGet();
                            }).onFailure((errMsg, cause) -> {
                            }).future()
                    ).start()
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
        SequentialSchedule<AtomicInteger> channelSchedule = new SequentialScheduleImpl<>(businessChannel1);
        B2BPipe<?, ?> pipe = businessChannel2.B2BPipe(businessChannel1);
        Blocker blocker = new Blocker();
        businessChannel1.execute(() -> {
            AtomicInteger input = new AtomicInteger();
            SequentialFuture<AtomicInteger> scheduleFuture = channelSchedule.withOutputContext(input)
                    .addTaskToTail(inputContext ->
                            pipe.request().messageType(messageType).onSuccess((outputValue) -> {
                                inputContext.incrementAndGet();
                            }).onFailure((errMsg, cause) -> {
                            }).future()
                    ).start();

            AtomicInteger counter = new AtomicInteger();
            for (int i = 0; i < 10; i++) {
                scheduleFuture
                        .onSuccess((outputValue) -> {
                            counter.incrementAndGet();
                            //real execution times is 1
                            blocker.assertEquals(outputValue.get(), 1)
                                    .assertEquals(outputValue.get(), 1);
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

    public void testSuccessOn() {
        BusinessChannel<?> businessChannel =
                APEF.createBusinessChannel(new ChannelConfig().setName("testSuccessOn"))
                        .start();
        SequentialSchedule<AtomicInteger> channelSchedule = new SequentialScheduleImpl<>(businessChannel);

        Blocker blocker = new Blocker();
        businessChannel.execute(() -> {
            AtomicInteger input = new AtomicInteger();
            channelSchedule.withOutputContext(input)
                    .successOn(outputContext -> outputContext.get() == 1)
                    .addTaskToTail(inputContext -> {
                        inputContext.incrementAndGet();
                        return ChannelFuture.completeFuture(true);
                    })
                    .addTaskToTail(inputContext -> {
                        inputContext.incrementAndGet();
                        return ChannelFuture.completeFuture(true);
                    })
                    .addTaskToTail(inputContext -> {
                        inputContext.incrementAndGet();
                        return ChannelFuture.completeFuture(true);
                    })
                    .start()
                    .onSuccess((outputValue) -> {
                        blocker.assertEquals(input.get(), 1)
                                .end();
                    })
                    .onFailure((errMsg, cause) -> {

                        blocker.failAndEnd("Should be success");
                    });
        });

        blocker.awaitEnd();
        businessChannel.close();
    }

    public void testFailureOn() {
        BusinessChannel<?> businessChannel =
                APEF.createBusinessChannel(new ChannelConfig().setName("testSuccessOn"))
                        .start();
        SequentialSchedule<AtomicInteger> channelSchedule = new SequentialScheduleImpl<>(businessChannel);

        Blocker blocker = new Blocker();
        businessChannel.execute(() -> {
            AtomicInteger input = new AtomicInteger();
            channelSchedule.withOutputContext(input)
                    .failureOn(outputContext -> outputContext.get() == 1)
                    .addTaskToTail(inputContext -> {
                        inputContext.incrementAndGet();
                        return ChannelFuture.completeFuture(true);
                    })
                    .addTaskToTail(inputContext -> {
                        inputContext.incrementAndGet();
                        return ChannelFuture.completeFuture(true);
                    })
                    .addTaskToTail(inputContext -> {
                        inputContext.incrementAndGet();
                        return ChannelFuture.completeFuture(true);
                    })
                    .start()
                    .onSuccess((outputValue) -> {
                        blocker.failAndEnd("Should be failure");
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker.assertEquals(input.get(), 1)
                                .end();
                    });
        });

        blocker.awaitEnd();
        businessChannel.close();
    }
}