package io.apef.core.channel.executor.schedule;

import io.apef.core.APEF;
import io.apef.core.channel.BusinessChannel;
import io.apef.core.channel.ChannelConfig;
import io.apef.core.channel.MessageType;
import io.apef.core.channel.future.ChannelFuture;
import io.apef.core.channel.message.ChannelMessageContext;
import io.apef.core.channel.pipe.B2BPipe;
import io.apef.testing.unit.BaseUnitSpec;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class ChannelScheduleImplBenchmark {
    @Test
    public void benchmarkSyncTasks() {
        BusinessChannel<?> businessChannel =
                APEF.createBusinessChannel(new ChannelConfig().setName("benchmarkSyncTasks"))
                        .start();
        ChannelSchedule<AtomicLong> channelSchedule = new ChannelScheduleImpl<>(businessChannel);
        BaseUnitSpec.Blocker blocker = new BaseUnitSpec.Blocker();
        AtomicLong counter = new AtomicLong();
        businessChannel.execute(() -> {
            try {
                long timeStart = System.currentTimeMillis();
                channelSchedule.withOutputContext(counter)
                        .times(-1)
                        .syncTask(true)
                        .submit(inputContext -> {
                            if (inputContext.incrementAndGet() % 10000000 == 0) {
                                log.info("TPS: " + inputContext.get() / (System.currentTimeMillis() - timeStart) + "," +
                                        "total: " + inputContext.get());
                            }
                            return ChannelFuture.completeFuture(true);
                        })
                        .onSuccess((outputValue) -> {
                            log.info("Total: " + outputValue.get());
                            blocker.end();
                        })
                        .onFailure((errMsg, cause) -> {
                            log.error(errMsg, cause);
                            blocker.failAndEnd("Should be success");
                        });
            } catch (Exception ex) {
                log.info("Failed to continue", ex);
            }
        });

        blocker.awaitEnd(3600000);
        businessChannel.close();

    }

    @Test
    public void benchmarkAsyncTasks() {
        MessageType messageType = MessageType.newType();
        BusinessChannel<?> businessChannel1 =
                APEF.createBusinessChannel(new ChannelConfig().setName("benchmarkAsyncTasks1"))
                        .start();

        BusinessChannel<?> businessChannel2 =
                APEF.createBusinessChannel(new ChannelConfig().setName("benchmarkAsyncTasks2"))
                        .handler(messageType, ChannelMessageContext::succeed)
                        .start();
        ChannelSchedule<AtomicInteger> channelSchedule = new ChannelScheduleImpl<>(businessChannel1);
        B2BPipe<?, ?> pipe = businessChannel2.B2BPipe(businessChannel1);

        BaseUnitSpec.Blocker blocker = new BaseUnitSpec.Blocker();
        AtomicInteger atomicInteger = new AtomicInteger();
        businessChannel1.execute(() -> {
            try {
                long timeStart = System.currentTimeMillis();
                channelSchedule.withOutputContext(atomicInteger)
                        .times(5000000)
                        .submit(inputContext -> {
                            return pipe.request().messageType(messageType)
                                    .requestContent(1)
                                    .onSuccess((outputValue) -> {
                                        if (inputContext.incrementAndGet() % 300000 == 0) {
                                            log.info("TPS: " + inputContext.get() / (System.currentTimeMillis() - timeStart) + "," +
                                                    "total: " + inputContext.get());
                                        }
                                    }).onFailure((errMsg, cause) -> {
                                    }).future();
                        })
                        .onSuccess((outputValue) -> {
                            log.info("Total: " + outputValue.get());
                            blocker.end();
                        })
                        .onFailure((errMsg, cause) -> {
                            log.error(errMsg, cause);
                            blocker.failAndEnd("Should be success");
                        });
            } catch (Exception ex) {
                log.info("Failed to continue", ex);
            }
        });

        blocker.awaitEnd(3600000);
        businessChannel1.close();
        businessChannel2.close();

    }

    public static void main(String args[]) {
        new ChannelScheduleImplBenchmark().benchmarkSyncTasks();
    }
}