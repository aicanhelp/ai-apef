package io.apef.core.channel.box;

import io.netty.util.concurrent.DefaultThreadFactory;
import org.testng.annotations.Test;

public class DisruptorMessageBoxBenchmark extends MessageBoxBenchmark {
    @Test
    public void benchmarkOne1() {
        doBenchmark(1, 1024);
    }

    @Test
    public void benchmarkOne2() {
        doBenchmark2(8096, 2000);
    }

    @Test
    public void benchmarkFourTypes() {
        doBenchmark(4, 1024);
    }

    @Test
    public void benchmarkSmallQueue() {
        doBenchmark(1, 256);
    }

    @Test
    public void benchmarkMidQueue() {
        doBenchmark(1, 2048);
    }

    @Test
    public void benchmarkLargeQueue() {
        doBenchmark(1, 8192);
    }


    private void doBenchmark(int typeCount, int queueSize) {
        super.doBenchmark(() -> new DisruptorMessageBox("testBox1", queueSize,
                1, new DefaultThreadFactory("Test")), typeCount);
    }

    private void doBenchmark2(int queueSize, int actors) {
        super.doBenchmark2(() -> new DisruptorMessageBox("testBox2", queueSize,
                1, new DefaultThreadFactory("Test")), actors);
    }
}
