package io.apef.testing.benchmark;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BenchmarkRunner {
    public interface IRunnerContext {
        void done(int index);
    }

    public interface ITestRunner extends IRunnerContext {
        void start();
    }

    public interface IBenchmarkTask {
        void run(int index, IRunnerContext runnerContext);
    }

    static class TestRunner implements ITestRunner {
        int fromIndex, toIndex;
        IBenchmarkTask benchmarkTask;
        volatile BenchmarkMetrics metrics;
        int next;
        boolean async;

        public TestRunner(int fromIndex, int toIndex,
                          boolean async,
                          IBenchmarkTask benchmarkTask) {
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
            this.benchmarkTask = benchmarkTask;
            next = this.fromIndex;
            this.async = async;
        }

        public void start() {
            this.run();
        }

        private void run() {
            if (this.async) {
                //log.info("run---------next: {}", this.next);
                if (this.next < this.toIndex) {
                    this.run(this.next);
                } else {
                    this.metrics.done(this.toIndex - this.fromIndex);
                }
            } else {
                for (int i = this.fromIndex; i < this.toIndex; i++) {
                    this.benchmarkTask.run(i, this);
                }
            }
        }

        private void run(int index) {
            this.beforeRun(index);
            this.benchmarkTask.run(index, this);
        }

        protected void beforeRun(int index) {
        }

        protected void afterRun(int index) {
        }

        @Override
        public void done(int index) {
            this.afterRun(index);
            this.next = index + 1;
            if (this.async) {
                this.run();
            } else {
                if (this.next == this.toIndex) {
                    this.metrics.done(this.toIndex - this.fromIndex);
                }
            }
            //log.info("Done-----" + index);
        }

        public synchronized TestRunner reset(BenchmarkMetrics metricTimer) {
            this.metrics = metricTimer;
            this.next = this.fromIndex;
            return this;
        }

        public synchronized int doneCount() {
            return this.next - this.fromIndex;
        }
    }

    static class MetricTestRunner extends TestRunner {

        public MetricTestRunner(int fromIndex, int toIndex,
                                boolean async,
                                IBenchmarkTask benchmarkTask) {
            super(fromIndex, toIndex, async, benchmarkTask);
        }

        @Override
        protected void beforeRun(int index) {
            this.metrics.timeStart(index);
        }

        @Override
        protected void afterRun(int index) {
            this.metrics.timeStop(index);
        }
    }

    protected static TestRunner createTestRunner(int fromIndex, int toIndex,
                                                 boolean async,
                                                 boolean detailMetrics,
                                                 IBenchmarkTask benchmarkTask) {
        if (detailMetrics) {
            return new MetricTestRunner(fromIndex, toIndex, async, benchmarkTask);
        } else
            return new TestRunner(fromIndex, toIndex, async, benchmarkTask);
    }
}
