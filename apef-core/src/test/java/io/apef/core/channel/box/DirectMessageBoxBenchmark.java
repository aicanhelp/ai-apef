package io.apef.core.channel.box;


import org.testng.annotations.Test;

public class DirectMessageBoxBenchmark extends MessageBoxBenchmark {
    @Test
    public void benchmarkOne() {
        doBenchmark(1);
    }

    @Test
    public void benchmarkFourTypes() {
        doBenchmark(4);
    }

    private void doBenchmark(int typeCount) {
        super.doBenchmark(() -> new DirectMessageBox("test"), typeCount);
    }
}
