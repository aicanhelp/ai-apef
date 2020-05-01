package io.apef.testing.unit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;


public class BaseUnitSpec implements UnitSpec {
    protected final static Logger log = LoggerFactory.getLogger(BaseUnitSpec.class);
    @Rule
    public TestName name = new TestName();

    @BeforeClass
    @org.junit.BeforeClass
    public void beforeClass() {
        MockitoAnnotations.initMocks(this);
        this.doBeforeClass();
    }

    protected void doBeforeClass() {

    }

    @Before
    public void before() throws Exception {
        log.info("Starting test: " + this.getClass().getSimpleName() + "#" + this.name.getMethodName());
        this.doBefore();
    }

    @AfterClass
    @org.junit.AfterClass
    public void afterClass() {
        this.doAfterClass();
    }


    protected void doAfterClass() {

    }

    protected void doBefore() {
    }

    @BeforeMethod
    public void beforeTest(Method method) throws Exception {
        log.info("Starting test: " + this.getClass().getSimpleName() + "#" + method.getName());
        this.doBefore();
    }

    public static void blockingSec(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (Exception ex) {
        }
    }

    public static void blockingMs(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ex) {
        }
    }

    protected static boolean blockingMsUntil(int ms, BooleanSupplier booleanSupplier, TimeoutAction timeoutAction) {
        long startTime = System.currentTimeMillis();
        log.info("Waiting result .....");
        while (!booleanSupplier.getAsBoolean()) {
            try {
                Thread.sleep(10);
                if (System.currentTimeMillis() - startTime > ms) {
                    timeoutAction.onTimeout();
                    return false;
                }
            } catch (Exception ex) {
            }
        }
        log.info("Done.");
        return true;
    }

    public interface TimeoutAction {
        void onTimeout();
    }

    protected static boolean blockingMsUntil(int ms, BooleanSupplier booleanSupplier) {
        long startTime = System.currentTimeMillis();
        while (!booleanSupplier.getAsBoolean()) {
            try {
                Thread.sleep(10);
                if (System.currentTimeMillis() - startTime > ms) return false;
            } catch (Exception ex) {
            }
        }
        return true;
    }

    protected static boolean blockingUntil(BooleanSupplier booleanSupplier) {
        return blockingMsUntil(30000, booleanSupplier);
    }

    protected static boolean blockingUntil(BooleanSupplier booleanSupplier, TimeoutAction timeoutAction) {
        return blockingMsUntil(30000, booleanSupplier, timeoutAction);
    }

    public static class Blocker {
        volatile boolean end;
        volatile long startTime = System.currentTimeMillis();
        volatile int errorCount = 0;
        private AtomicInteger times = new AtomicInteger();

        public Blocker reset() {
            this.end = false;
            this.errorCount = 0;
            this.startTime = System.currentTimeMillis();
            this.times = new AtomicInteger();
            return this;
        }

        public Blocker awaitEnd() {
            if (!BaseUnitSpec.blockingMsUntil(30000, () -> end)) {
                this.failAndEnd("Wait End Timeout");
            }
            this.conclude();
            return this;
        }

        public int times() {
            return this.times.get();
        }

        public Blocker awaitEnd(int ms) {
            if (!BaseUnitSpec.blockingMsUntil(ms, () -> end)) {
                this.failAndEnd("Wait End Timeout");
            }
            this.conclude();
            return this;
        }

        public Blocker awaitAndEnd(int ms) {
            BaseUnitSpec.blockingMsUntil(ms, () -> end);
            this.conclude();
            return this;
        }

        public Blocker awaitTimes(int expectedTimes) {
            if (!BaseUnitSpec.blockingMsUntil(30000, () -> times.get() == expectedTimes)) {
                this.failAndEnd("Wait End Timeout");
            }
            return this;
        }

        public Blocker awaitTimesEnd(int expectedTimes) {
            if (!BaseUnitSpec.blockingMsUntil(30000, () -> times.get() == expectedTimes)) {
                this.failAndEnd("Wait End Timeout");
            }
            this.conclude();
            return this;
        }

        public Blocker timeInc() {
            this.times.incrementAndGet();
            return this;
        }

        public Blocker timeInc(int value) {
            this.times.addAndGet(value);
            return this;
        }

        public Blocker awaitMs(int ms) {
            BaseUnitSpec.blockingMs(ms);
            return this;
        }

        public Blocker awaitSec(int secs) {
            BaseUnitSpec.blockingSec(secs);
            return this;
        }

        public Blocker awaitEnd(EndAction endAction) {
            if (!BaseUnitSpec.blockingMsUntil(30000, () -> end)) {
                this.failAndEnd("Wait End Timeout");
            }
            endAction.doEnd();
            this.conclude();
            return this;
        }

        public Blocker awaitEnd(int ms, EndAction endAction) {
            if (!BaseUnitSpec.blockingMsUntil(ms, () -> end)) {
                this.failAndEnd("Wait End Timeout");
            }
            endAction.doEnd();
            this.conclude();
            return this;
        }

        public Blocker awaitMs(int ms, EndAction endAction) {
            this.awaitMs(ms);
            endAction.doEnd();
            return this;
        }

        public Blocker awaitSec(int sec, EndAction endAction) {
            this.awaitSec(sec);
            endAction.doEnd();
            return this;
        }

        public long end() {
            end = true;
            return System.currentTimeMillis() - startTime;
        }

        public Blocker fail(String msg) {
            log.error(msg);
            this.errorCount = errorCount + 1;
            return this;
        }

        public Blocker fail(String msg, Throwable ex) {
            log.error(msg, ex);
            this.errorCount = errorCount + 1;
            return this;
        }

        public Blocker endIf(BooleanSupplier supplier) {
            if (supplier.getAsBoolean()) {
                this.end();
            }
            return this;
        }

        public Blocker endIf(boolean value) {
            if (value) {
                this.end();
            }
            return this;
        }

        public Blocker failAndEnd(String msg) {
            this.fail(msg);
            this.end();
            return this;
        }

        public Blocker failAndEnd(String msg, Throwable ex) {
            this.fail(msg, ex).end();
            return this;
        }

        public Blocker timeIn(long time) {
            long t = System.currentTimeMillis() - startTime;
            return this.verify(() -> assertTrue(t < time));
        }

        public void endIn(long time) {
            long t = System.currentTimeMillis() - startTime;
            this.verify(() -> assertTrue(t < time)).end();
        }

        public Blocker timeIn(long min, long max) {
            long time = System.currentTimeMillis() - startTime;
            return this.verify(() -> {
                assertTrue(time > min);
                assertTrue(time < max);
            });
        }

        public void endIn(long min, long max) {
            long time = System.currentTimeMillis() - startTime;
            this.verify(() -> {
                assertTrue(time > min);
                assertTrue(time < max);
            }).end();
        }

        public Blocker timeOut(long time) {
            long t = System.currentTimeMillis() - startTime;
            return this.verify(() -> assertTrue(t > time));
        }

        public void endOut(long time) {
            long t = System.currentTimeMillis() - startTime;
            this.verify(() -> assertTrue(t > time)).end();
        }

        public Blocker assertNotEquals(Object o1, Object o2) {
            this.verify(() -> Assert.assertNotEquals(o1, o2));
            return this;
        }

        public Blocker assertEquals(Object o1, Object o2) {
            this.verify(() -> Assert.assertEquals(o1, o2));
            return this;
        }

        public Blocker assertTrue(boolean value) {
            this.verify(() -> Assert.assertTrue(value));
            return this;
        }

        public Blocker assertNotNull(Object value) {
            this.verify(() -> Assert.assertNotNull(value));
            return this;
        }

        public Blocker assertNull(Object value) {
            this.verify(() -> Assert.assertNull(value));
            return this;
        }

        public Blocker assertFalse(boolean value) {
            this.verify(() -> Assert.assertFalse(value));
            return this;
        }

        public Blocker assertTimes(int expectedTimes) {
            this.verify(() -> Assert.assertEquals(this.times.get(), expectedTimes));
            return this;
        }

        public Blocker verify(CheckAction checkAction) {
            try {
                checkAction.check();
            } catch (Error error) {
                log.error("", error);
                errorCount = errorCount + 1;
            }
            return this;
        }

        public void conclude() {
            if (this.errorCount > 0) {
                Assert.fail();
            }
        }

        public interface EndAction {
            void doEnd();
        }

        public interface CheckAction {
            void check();
        }
    }
}
