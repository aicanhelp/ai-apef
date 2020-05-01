package io.apef.core.channel.executor.streaming;

import io.apef.core.APEF;
import io.apef.core.channel.BusinessChannel;
import io.apef.core.channel.ChannelConfig;
import io.apef.core.channel.ClientChannel;
import io.apef.core.channel.MessageType;
import io.apef.core.channel.executor.schedule.ChannelSchedule;
import io.apef.core.channel.executor.schedule.ChannelScheduleImpl;
import io.apef.core.channel.future.ChannelFuture;
import io.apef.core.channel.future.ChannelFutureImpl;
import io.apef.core.channel.pipe.B2CPipe;
import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Test
public class ChannelStreamImplTest extends BaseUnitSpec {
    public void testStreamFuture() {
        ChannelFutureImpl<Integer> firstFuture =
                new ChannelFutureImpl<>();

        ChannelStreamImpl<Integer> channelStream = new ChannelStreamImpl<>(firstFuture);

        firstFuture.complete(1);

        Blocker blocker = new Blocker();
        channelStream.future().onFailure((errMsg, cause) -> {
            blocker.failAndEnd("should be success");
        }).onSuccess(outputValue -> {
            blocker.assertEquals(outputValue, 1).end();
        });

        blocker.awaitEnd();
    }

    public void testSuccessStreaming() {
        ChannelFutureImpl<Integer> future1 =
                new ChannelFutureImpl<>();
        ChannelFutureImpl<String> future2 =
                new ChannelFutureImpl<>();
        ChannelFutureImpl<Boolean> future3 =
                new ChannelFutureImpl<>();

        ChannelStreamImpl<Integer> channelStream = new ChannelStreamImpl<>(future1);

        Blocker blocker = new Blocker();
        channelStream.map(value -> future2)
                .map(null, (errMsg, ex) -> future3)
                .map(value -> null, (errMsg, ex) -> null)
                .future()
                .onSuccess(outputValue -> {
                    blocker.end();
                })
                .onFailure((errMsg, cause) -> {
                    blocker.failAndEnd("should be success");
                });

        future1.complete(0);
        future2.complete("", null);
        future3.complete(true);

        blocker.awaitEnd();
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

        businessChannel.execute(() -> {
            AtomicInteger input = new AtomicInteger();
            channelSchedule.withOutputContext(input)
                    .timeUnit(TimeUnit.MILLISECONDS)
                    .times(4)
                    .completeOnSuccess(true)
                    .submit(inputContext -> {
                        counter.incrementAndGet();
                        return businessChannel.stream(
                                ChannelFuture.completeFuture(9999)
                        ).map(value -> pipe.request()
                                .retry(2, 100)
                                .messageType(messageType)
                                .requestContent(value).future()
                        ).future()
                                .onSuccess(outputValue -> {
                                    blocker.assertEquals(outputValue, 9999);
                                });
                    })
                    .onSuccess((outputValue) -> {
                        blocker.assertEquals(counter.get(), 2)
                                //run times= one time failures+2 retries failures+one time success=4
                                .assertEquals(counter2.get(), 4)
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

}