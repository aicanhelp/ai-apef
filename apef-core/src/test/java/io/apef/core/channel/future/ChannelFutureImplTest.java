package io.apef.core.channel.future;

import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;

@Test
public class ChannelFutureImplTest extends BaseUnitSpec {
    public void testFuture() {
        ChannelFutureImpl<Integer> future1 =
                new ChannelFutureImpl<>();

        Blocker blocker = new Blocker();
        future1.onFailure((errMsg, cause) -> {
            blocker.failAndEnd("should be success");
        }).onSuccess(outputValue -> {
            blocker.assertEquals(outputValue, 1).end();
        });

        future1.complete(1);
        blocker.awaitEnd();

        Blocker blocker2 = new Blocker();

        for (int i = 0; i < 10; i++) {
            future1.onFailure((errMsg, cause) -> {
                blocker2.failAndEnd("should be success");
            }).onSuccess(outputValue -> {
                blocker2.timeInc().endIf(blocker2.times() == 10);
            });
        }
        blocker2.awaitEnd();
    }

    public void testFollow() {
        ChannelFutureImpl<Integer> future1 =
                new ChannelFutureImpl<>();
        ChannelFutureImpl<Integer> future2 =
                new ChannelFutureImpl<>();

        future1.complete(1);
        Blocker blocker1 = new Blocker();
        //test follow future
        future2.follow(future1);
        future2.onFailure((errMsg, cause) -> {
            blocker1.failAndEnd("should be success");
        }).onSuccess(outputValue -> {
            blocker1.assertEquals(outputValue, 1).end();
        });
        blocker1.awaitEnd();

        Blocker blocker2 = new Blocker();

        for (int i = 0; i < 10; i++) {
            future2.onFailure((errMsg, cause) -> {
                blocker2.failAndEnd("should be success");
            }).onSuccess(outputValue -> {
                blocker2.timeInc().endIf(blocker2.times() == 10);
            });
        }
        blocker2.awaitEnd();
    }

}