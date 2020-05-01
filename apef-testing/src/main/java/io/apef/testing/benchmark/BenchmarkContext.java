package io.apef.testing.benchmark;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Data
@Accessors(fluent = true)
@Slf4j
public class BenchmarkContext {

    String name = "Default";
    int threads = 1;
    /**
     * Here the concurrency is the number of actors,
     * Generally, the number can be set 1000
     */
    int concurrency = 1;
    /**
     * The total iterations=iterations*concurrency
     * Generally, the number can be set as 10000
     */
    int iterations = 1000;
    int rounds = 3;
    int warmupIterations;
    int warmupRounds;
    int warmupConcurrency;
    int reportInterval = 2;
    boolean defailMetrics;
    int timeout = 30000;
    boolean async = true;
    BenchmarkRunner.IBenchmarkTask benchmarkTask;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private ExecutorService executorService;

    private void checkConfig() {
        String params = System.getProperty("benchmarkParams");

        if (!StringUtils.isEmpty(params)) {
            try {
                String[] paramArrays = StringUtils.split(params, ',');
                this.threads = Integer.valueOf(paramArrays[0]);
                if (paramArrays.length > 1) {
                    this.concurrency = Integer.valueOf(paramArrays[1]);
                }
                if (paramArrays.length > 2) {
                    this.iterations = Integer.valueOf(paramArrays[2]);
                }
                if (paramArrays.length > 3) {
                    this.rounds = Integer.valueOf(paramArrays[3]);
                }
                if (paramArrays.length > 4) {
                    this.warmupConcurrency = Integer.valueOf(paramArrays[4]);
                }
                if (paramArrays.length > 5) {
                    this.warmupIterations = Integer.valueOf(paramArrays[5]);
                }
                if (paramArrays.length > 5) {
                    this.warmupRounds = Integer.valueOf(paramArrays[6]);
                }
            } catch (Exception ex) {
                throw new IllegalArgumentException("Failed to parse input benchmark params: " + params, ex);
            }
        }

        this.threads = Integer.getInteger("threads", this.threads);
        this.concurrency = Integer.getInteger("concurrency", this.concurrency);
        this.iterations = Integer.getInteger("iterations", this.iterations);
        this.rounds = Integer.getInteger("rounds", this.rounds);
        this.warmupConcurrency = Integer.getInteger("warmupConcurrency", this.warmupConcurrency);
        this.warmupIterations = Integer.getInteger("warmupIterations", this.warmupIterations);
        this.warmupRounds = Integer.getInteger("warmupRounds", this.warmupRounds);

        this.concurrency = concurrency > 0 ? concurrency : 1;
        executorService = Executors.newFixedThreadPool(threads);
        checkTotalIterations(concurrency, iterations);
        checkTotalIterations(warmupConcurrency, warmupIterations);
    }


    private void checkTotalIterations(int concurrency, int iterations) {
        if (concurrency == 0 || iterations == 0) return;
        int eachIterations = Integer.MAX_VALUE / concurrency;

        if (iterations > eachIterations) {
            throw new IllegalArgumentException("Iterations can not be >" + eachIterations
                    + ": concurrency * iterations can not >" + Integer.MAX_VALUE);
        }

        if (concurrency > 3000 || (concurrency * iterations > 30000000)) {
            log.warn("The total iterations may be too large: \r\n" +
                    "totalIterations=concurrency*iterations= " + concurrency + "*" + iterations + "=" + (concurrency * iterations));
            log.warn("The test may not be abled to finish. You may need to decrease the number of concurrency or iterations.");
            log.warn("If the test can be finished. You can ignore this warning.");
        }
    }

    public void start() {
        this.checkConfig();

        if (warmupRounds > 0 && warmupIterations > 0) {
            int setup = warmupConcurrency / warmupRounds;
            if (setup == 0) setup = 1;
            int eachConcurrency = 0;
            for (int i = 0; i < warmupRounds; i++) {
                eachConcurrency = eachConcurrency + setup;
                runTest(name + "_Warmup_" + i, eachConcurrency, warmupIterations, 1);
            }
        }
        if (rounds > 0 && iterations > 0)
            runTest(name + "_Benchmark", concurrency, iterations, this.rounds);
        executorService.shutdown();
    }

    protected void runTest(String name, int concurrency, int iterations, int rounds) {
        List<BenchmarkRunner.TestRunner> testRunners = new ArrayList<>();

        List<Future> tasks = new ArrayList<>();
        int fromIndex = 0, toIndex = 0;

        for (int i = 0; i < concurrency; i++) {
            toIndex = fromIndex + iterations;

            testRunners.add(BenchmarkRunner.createTestRunner(fromIndex, toIndex,
                    this.async,
                    defailMetrics, this.benchmarkTask));
            fromIndex = toIndex;
        }

        for (int i = 0; i < rounds; i++) {
            tasks.forEach(future -> {
                future.cancel(true);
            });
            tasks.clear();
            final BenchmarkMetrics metricTimer = new BenchmarkMetrics(name + "_Round_" + i, concurrency,
                    toIndex, this.defailMetrics, this.reportInterval);
            metricTimer.start();

            testRunners.forEach(iTestRunner -> {
                iTestRunner.reset(metricTimer);
                tasks.add(this.executorService.submit(iTestRunner::start));
            });

            metricTimer.waitFinished(this.timeout);
        }

    }
}
