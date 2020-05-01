package io.apef.core.channel.executor.batch;

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
public class BatchScheduleImplTest extends BaseUnitSpec {
    @Override
    protected void doBeforeClass() {
        ChannelTimer.timer();
    }

    public void testOneTask1() {
        BusinessChannel<?> businessChannel =
                APEF.createBusinessChannel(new ChannelConfig().setName("testOneTask1"))
                        .start();
        BatchSchedule<Integer> batchSchedule = new BatchScheduleImpl<>(businessChannel);
        Blocker blocker = new Blocker();
        businessChannel.execute(() -> {
            batchSchedule.withOutputContext(1)
                    .task("task1")
                    .submit(inputContext -> ChannelFuture.completeFuture(true))
                    .start()
                    .onSuccess((outputValue) -> {
                        blocker.assertEquals(outputValue, 1)
                                .assertEquals(outputValue, 1).end();
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker.failAndEnd("should success");
                    });
        });

        blocker.awaitEnd();
        businessChannel.close();
    }

    public void testOneTask2() {

        BusinessChannel<?> businessChannel1 = APEF.createBusinessChannel(new ChannelConfig().setName("testOneTask2_1"))
                .start();

        MessageType messageType = MessageType.newType();
        BusinessChannel<?> businessChannel2 = APEF.createBusinessChannel(new ChannelConfig().setName("testOneTask2_2"))
                .handler(messageType, (messageContext, requestContent) -> {
                    messageContext.succeed(requestContent);
                })
                .start();

        B2BPipe<?, ?> b2BPipe = businessChannel2.B2BPipe(businessChannel1);
        BatchSchedule<Integer> batchSchedule = new BatchScheduleImpl<>(businessChannel1);

        Blocker blocker = new Blocker();
        businessChannel1.execute(() -> {
            batchSchedule.withOutputContext(1)
                    .task("task1")
                    .submit(inputContext -> {
                        return b2BPipe.request()
                                .messageType(messageType)
                                .onSuccess((outputValue) -> {
                                })
                                .onFailure((errMsg, cause) -> {
                                })
                                .future();
                    })
                    .start()
                    .onSuccess((outputValue) -> {
                        blocker.assertEquals(outputValue, 1).end();
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker.failAndEnd("should success");
                    });
        });

        blocker.awaitEnd();
        businessChannel1.close();
        businessChannel2.close();
    }

    public void testMultiTasks1() {
        BusinessChannel<?> businessChannel =
                APEF.createBusinessChannel(new ChannelConfig().setName("testMultiTasks1"))
                        .start();
        BatchSchedule<AtomicInteger> batchSchedule = new BatchScheduleImpl<>(businessChannel);
        Blocker blocker = new Blocker();
        AtomicInteger value = new AtomicInteger();
        businessChannel.execute(() -> {
            batchSchedule.withOutputContext(value)
                    .task("task1")
                    .submit(inputContext -> {
                        inputContext.incrementAndGet();
                        return ChannelFuture.completeFuture(true);
                    })
                    .task("task2")
                    .submit(inputContext -> {
                        inputContext.incrementAndGet();
                        return ChannelFuture.completeFuture(true);
                    })
                    .task("task3")
                    .submit(inputContext -> {
                        inputContext.incrementAndGet();
                        return ChannelFuture.completeFuture(true);
                    })
                    .start()
                    .onSuccess((outputValue) -> {
                        blocker.assertEquals(outputValue.get(), 3).end();
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker.failAndEnd("should success");
                    });
        });

        blocker.awaitEnd();
        businessChannel.close();
    }

    public void testMultiTasks2() {

        BusinessChannel<?> businessChannel1 = APEF.createBusinessChannel(new ChannelConfig().setName("testMultiTasks2_1"))
                .start();

        MessageType messageType = MessageType.newType();
        BusinessChannel<?> businessChannel2 = APEF.createBusinessChannel(new ChannelConfig().setName("testMultiTasks2_2"))
                .handler(messageType, (messageContext, requestContent) -> {
                    messageContext.succeed(requestContent);
                })
                .start();

        B2BPipe<?, ?> b2BPipe = businessChannel2.B2BPipe(businessChannel1);
        BatchSchedule<AtomicInteger> batchSchedule = new BatchScheduleImpl<>(businessChannel1);

        AtomicInteger value = new AtomicInteger();
        Blocker blocker = new Blocker();
        businessChannel1.execute(() -> {
            batchSchedule.withOutputContext(value)
                    .task("task1")
                    .submit(inputContext -> {
                        return b2BPipe.request()
                                .messageType(messageType)
                                .onSuccess((outputValue) -> {
                                    inputContext.incrementAndGet();
                                })
                                .onFailure((errMsg, cause) -> {
                                })
                                .future();
                    })
                    .task("task2")
                    .submit(inputContext -> {
                        return b2BPipe.request()
                                .messageType(messageType)
                                .onSuccess((outputValue) -> {
                                    inputContext.incrementAndGet();
                                })
                                .onFailure((errMsg, cause) -> {
                                })
                                .future();
                    })
                    .task("task3")
                    .submit(inputContext -> {
                        return b2BPipe.request()
                                .messageType(messageType)
                                .onSuccess((outputValue) -> {
                                    inputContext.incrementAndGet();
                                })
                                .onFailure((errMsg, cause) -> {
                                })
                                .future();
                    })
                    .start()
                    .onSuccess((outputValue) -> {
                        blocker.assertEquals(outputValue.get(), 3).end();
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker.failAndEnd("should success");
                    });
        });

        blocker.awaitEnd();
        businessChannel1.close();
        businessChannel2.close();
    }

    public void testTimeout() {
        BusinessChannel<?> businessChannel1 = APEF.createBusinessChannel(new ChannelConfig().setName("testMultiTasks2_1"))
                .start();

        MessageType messageType = MessageType.newType();
        BusinessChannel<?> businessChannel2 = APEF.createBusinessChannel(new ChannelConfig().setName("testMultiTasks2_2"))
                .handler(messageType, (messageContext, requestContent) -> {
                    blockingMs(100);
                    messageContext.succeed(requestContent);
                })
                .start();

        B2BPipe<?, ?> b2BPipe = businessChannel2.B2BPipe(businessChannel1);
        BatchSchedule<AtomicInteger> batchSchedule = new BatchScheduleImpl<>(businessChannel1);

        AtomicInteger value = new AtomicInteger();
        Blocker blocker = new Blocker();
        businessChannel1.execute(() -> {
            batchSchedule.withOutputContext(value)
                    .timeout(1)
                    .timeUnit(TimeUnit.MILLISECONDS)
                    .task("task1")
                    .submit(inputContext -> {
                        return b2BPipe.request()
                                .messageType(messageType)
                                .onSuccess((outputValue) -> {
                                    inputContext.incrementAndGet();
                                })
                                .onFailure((errMsg, cause) -> {
                                })
                                .future();
                    })
                    .task("task2")
                    .submit(inputContext -> {
                        return b2BPipe.request()
                                .messageType(messageType)
                                .onSuccess((outputValue) -> {
                                    inputContext.incrementAndGet();
                                })
                                .onFailure((errMsg, cause) -> {
                                })
                                .future();
                    })
                    .task("task3")
                    .submit(inputContext -> {
                        return b2BPipe.request()
                                .messageType(messageType)
                                .onSuccess((outputValue) -> {
                                    inputContext.incrementAndGet();
                                })
                                .onFailure((errMsg, cause) -> {
                                })
                                .future();
                    })
                    .start()
                    .onSuccess((outputValue) -> {
                        blocker.failAndEnd("should be failure");
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

    public void testFuture1() {
        BusinessChannel<?> businessChannel = APEF.createBusinessChannel(new ChannelConfig().setName("testFuture"))
                .start();
        BatchSchedule<Integer> batchSchedule = new BatchScheduleImpl<>(businessChannel);
        Blocker blocker = new Blocker();
        businessChannel.execute(() -> {
            BatchFuture<Integer> batchFuture = batchSchedule.withOutputContext(1)
                    .task("task1")
                    .submit(inputContext -> ChannelFuture.completeFuture(true))
                    .start();
            AtomicInteger counter = new AtomicInteger();
            for (int i = 0; i < 10; i++) {
                batchFuture
                        .onSuccess((outputValue) -> {
                            counter.incrementAndGet();
                            blocker.assertEquals(outputValue, 1).endIf(counter.get() == 10);
                        })
                        .onFailure((errMsg, cause) -> {
                            blocker.failAndEnd("should success");
                        });
            }
        });

        blocker.awaitEnd();
        businessChannel.close();
    }


    public void testFuture2() {
        BusinessChannel<?> businessChannel1 = APEF.createBusinessChannel(new ChannelConfig().setName("testFuture2_1"))
                .start();

        MessageType messageType = MessageType.newType();
        BusinessChannel<?> businessChannel2 = APEF.createBusinessChannel(new ChannelConfig().setName("testFuture2_2"))
                .handler(messageType, ChannelMessageContext::succeed)
                .start();

        B2BPipe<?, ?> b2BPipe = businessChannel2.B2BPipe(businessChannel1);

        Blocker blocker = new Blocker();
        BatchSchedule<Integer> batchSchedule = new BatchScheduleImpl<>(businessChannel1);
        businessChannel1.execute(() -> {
            BatchFuture<Integer> batchFuture = batchSchedule.withOutputContext(1)
                    .task("task1")
                    .submit(inputContext -> b2BPipe.request()
                            .messageType(messageType)
                            .onSuccess((outputValue) -> {
                            })
                            .onFailure((errMsg, cause) -> {
                            })
                            .future())
                    .start();
            AtomicInteger counter = new AtomicInteger();
            for (int i = 0; i < 10; i++) {
                batchFuture
                        .onSuccess((outputValue) -> {
                            counter.incrementAndGet();
                            blocker.assertEquals(outputValue, 1).endIf(counter.get() == 10);
                        })
                        .onFailure((errMsg, cause) -> {
                            blocker.failAndEnd("should success");
                        });
            }
            blocker.end();
        });

        blocker.awaitEnd();
        businessChannel1.close();
        businessChannel2.close();
    }

    public void testConcurrency() {
        BusinessChannel<?> businessChannel1 = APEF.createBusinessChannel(new ChannelConfig().setName("testConcurrency1"))
                .start();

        MessageType messageType = MessageType.newType();
        BusinessChannel<?> businessChannel2 = APEF.createBusinessChannel(new ChannelConfig().setName("testConcurrency2"))
                .handler(messageType, (messageContext, requestContent) -> {
                    blockingMs(100);
                    messageContext.succeed(requestContent);
                })
                .start();

        B2BPipe<?, ?> b2BPipe = businessChannel2.B2BPipe(businessChannel1);

        Blocker blocker = new Blocker();
        BatchSchedule<Integer> batchSchedule = new BatchScheduleImpl<>(businessChannel1);
        businessChannel1.execute(() -> {
            AtomicInteger value = new AtomicInteger();
            batchSchedule.concurrency(1).withOutputContext(1);

            for (int i = 0; i < 3; i++) {
                batchSchedule.task("task_" + i)
                        .submit(inputContext -> b2BPipe.request()
                                .messageType(messageType)
                                .onSuccess((outputValue) -> {
                                })
                                .onFailure((errMsg, cause) -> {
                                })
                                .future());
            }

            batchSchedule.start().onSuccess((outputValue) -> {
                //In fact,the executing should should >100*3
                blocker.endOut(250);
            }).onFailure((errMsg, cause) -> {
                blocker.failAndEnd("should success");
            });
        });

        blocker.awaitEnd();
        businessChannel1.close();
        businessChannel2.close();
    }

    public void testStopOnFailure1() {
        BusinessChannel<?> businessChannel = APEF.createBusinessChannel(new ChannelConfig().setName("testStopOnFailure1"))
                .start();

        Blocker blocker = new Blocker();
        BatchSchedule<AtomicInteger> batchSchedule = new BatchScheduleImpl<>(businessChannel);
        businessChannel.execute(() -> {
            AtomicInteger value = new AtomicInteger();
            batchSchedule.withOutputContext(value)
                    .concurrency(1)
                    .stopOnFailure(true)
                    .task("task1")
                    .submit(inputContext -> {
                        value.incrementAndGet();
                        return ChannelFuture.completeFuture(false);
                    })
                    .task("task2")
                    .submit(inputContext -> {
                                value.incrementAndGet();
                                return ChannelFuture.completeFuture(false);
                            }
                    ).start().onSuccess((outputValue) -> {
                blocker.failAndEnd("should failed");
            }).onFailure((errMsg, cause) -> {
                blocker.assertEquals(value.get(), 1).end();
            });
        });

        blocker.awaitEnd();
        businessChannel.close();
    }

    public void testStopOnFailure2() {
        BusinessChannel<?> businessChannel = APEF.createBusinessChannel(new ChannelConfig().setName("testStopOnFailure1"))
                .start();

        Blocker blocker = new Blocker();
        BatchSchedule<AtomicInteger> batchSchedule = new BatchScheduleImpl<>(businessChannel);
        businessChannel.execute(() -> {
            AtomicInteger value = new AtomicInteger();
            batchSchedule.withOutputContext(value)
                    .concurrency(1)
                    .task("task1")
                    .stopOnFailure(true)
                    .submit(inputContext -> {
                        value.incrementAndGet();
                        return ChannelFuture.completeFuture(false);
                    })
                    .task("task2")
                    .stopOnFailure(true)
                    .submit(inputContext -> {
                                value.incrementAndGet();
                                return ChannelFuture.completeFuture(false);
                            }
                    ).start().onSuccess((outputValue) -> {
                blocker.failAndEnd("should failed");
            }).onFailure((errMsg, cause) -> {
                blocker.assertEquals(value.get(), 1).end();
            });
        });

        blocker.awaitEnd();
        businessChannel.close();
    }

    public void testSuccessOn() {
        BusinessChannel<?> businessChannel =
                APEF.createBusinessChannel(new ChannelConfig().setName("testSuccessOn"))
                        .start();
        BatchSchedule<AtomicInteger> batchSchedule = new BatchScheduleImpl<>(businessChannel);
        Blocker blocker = new Blocker();
        AtomicInteger value = new AtomicInteger();

        businessChannel.execute(() -> {
            batchSchedule.withOutputContext(value)
                    .concurrency(3)
                    .successOn(outputContext -> outputContext.get() == 1)
                    .task("task1")
                    .submit(inputContext -> {
                        inputContext.incrementAndGet();
                        return ChannelFuture.completeFuture(true);
                    })
                    .task("task2")
                    .submit(inputContext -> {
                        inputContext.incrementAndGet();
                        return ChannelFuture.completeFuture(true);
                    })
                    .task("task3")
                    .submit(inputContext -> {
                        inputContext.incrementAndGet();
                        return ChannelFuture.completeFuture(true);
                    })
                    .start()
                    .onSuccess((outputValue) -> {
                        blocker.assertEquals(outputValue.get(), 1).end();
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker.failAndEnd("should success");
                    });
        });

        blocker.awaitEnd();
        businessChannel.close();
    }

    public void testFailureOn() {
        BusinessChannel<?> businessChannel =
                APEF.createBusinessChannel(new ChannelConfig().setName("testFailureOn"))
                        .start();
        BatchSchedule<AtomicInteger> batchSchedule = new BatchScheduleImpl<>(businessChannel);
        Blocker blocker = new Blocker();
        AtomicInteger value = new AtomicInteger();
        businessChannel.execute(() -> {
            batchSchedule.withOutputContext(value)
                    .concurrency(3)
                    .failureOn(outputContext -> outputContext.get() == 1)
                    .task("task1")
                    .submit(inputContext -> {
                        inputContext.incrementAndGet();
                        return ChannelFuture.completeFuture(true);
                    })
                    .task("task2")
                    .submit(inputContext -> {
                        inputContext.incrementAndGet();
                        return ChannelFuture.completeFuture(true);
                    })
                    .task("task3")
                    .submit(inputContext -> {
                        inputContext.incrementAndGet();
                        return ChannelFuture.completeFuture(true);
                    })
                    .start()
                    .onSuccess((outputValue) -> {
                        blocker.failAndEnd("should failure");
                    })
                    .onFailure((errMsg, cause) -> {
                        blocker.assertEquals(value.get(), 1).end();
                    });
        });

        blocker.awaitEnd();
        businessChannel.close();
    }
}