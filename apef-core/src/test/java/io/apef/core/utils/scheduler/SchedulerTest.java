package io.apef.core.utils.scheduler;

import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.testng.Assert.*;

public class SchedulerTest extends BaseUnitSpec {
    @Test
    public void testTimes() {
        Blocker blocker = new Blocker();
        Scheduler.schedule()
                .delay(0)
                .interval(1)
                .times(10)
                .scheduleTask(scheduleContext -> {
                    blocker.timeInc();
                }).start();

        blocker.awaitMs(200).assertTimes(10).conclude();
    }

    @Test
    public void testInterval() {
        Blocker blocker = new Blocker();
        long startTime = System.currentTimeMillis();

        Scheduler.schedule()
                .delay(0)
                .interval(100)
                .times(10)
                .scheduleTask(scheduleContext -> {
                    blocker.timeInc();
                }).start();

        blocker.awaitTimes(10);
        long endTime = System.currentTimeMillis();
        assertTrue((endTime - startTime + 100) > 100 * 10);

    }

    @Test
    public void testDelay() {
        Blocker blocker = new Blocker();
        long startTime = System.currentTimeMillis();

        Scheduler.schedule()
                .delay(300)
                .times(1)
                .scheduleTask(scheduleContext -> {
                    blocker.timeInc();
                }).start();

        blocker.awaitTimes(1);
        long endTime = System.currentTimeMillis();
        assertTrue((endTime - startTime) > 300);
    }

    @Test
    public void testCustomEnd() {
        Blocker blocker = new Blocker();

        Scheduler.schedule()
                .times(20)
                .scheduleTask(scheduleContext -> {
                    blocker.timeInc();
                    if (blocker.times() == 10) {
                        scheduleContext.end();
                        blocker.end();
                    }
                }).start();


        blocker.awaitMs(200).awaitEnd();
        assertEquals(blocker.times(), 10);
    }

    @Test
    public void testRetry1() {
        Blocker blocker = new Blocker();
        Scheduler.retry()
                .delay(0)
                .interval(10)
                .times(10)
                .scheduleTask(scheduleContext -> {
                    if (scheduleContext.retry()) {
                        blocker.timeInc();
                    }
                }).start();

        blocker.awaitTimes(10);
    }

    @Test
    public void testRetry2() {
        Blocker blocker = new Blocker();
        Scheduler.retry()
                .delay(0)
                .interval(10)
                .times(10)
                .scheduleTask(scheduleContext -> {
                    if (blocker.timeInc().times() < 6) {
                        scheduleContext.retry();
                    } else {
                        scheduleContext.end();
                    }
                }).start();

        blocker.awaitTimes(6).awaitMs(200).assertTimes(6).end();
    }

    @Test
    public void testCustomEnd2() {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        for (int i = 0; i < 10; i++) {
            Blocker blocker = new Blocker();
            Scheduler.schedule()
                    .times(10)
                    .scheduleTask(scheduleContext -> {
                        executorService.submit(() -> {
                            blocker.timeInc();
                            if (scheduleContext.timeIndex() == 5) {
                                scheduleContext.end();
                                blocker.end();
                            }
                            if (scheduleContext.timeIndex() > 5) {
                                blocker.failAndEnd("Should't execute it");
                            }
                        });
                    }).start();

            blocker.awaitMs(100).assertTimes(5).awaitEnd();
        }

        executorService.shutdownNow();
    }
}