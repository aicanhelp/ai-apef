package io.apef.metrics;

import com.codahale.metrics.Timer;
import io.apef.metrics.item.*;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ApefMetricsFactoryTest extends BaseUnitSpec {

    @Test
    public void testSlf4jReporter() {
        ApefMetrics metrics = ApefMetricsFactory.defaultMetrics();

        metrics.vexMetric("testMetric").counter("counters").inc();
        metrics.report("testMetric");
    }

    @Test
    public void testBuildAndConfig() {
        ApefMetrics metrics = ApefMetricsFactory.defaultMetrics();
        ApefMetric apefMetric = metrics.vexMetric("testMetric");
        verifyMetric(apefMetric, 1);

        ApefMetric apefMetric1 = metrics.vexMetric("testMetric1");
        verifyMetric(apefMetric1, 0);

        apefMetric1.timer("get").enabled(true);
        apefMetric1.counter("get").enabled(true);
        apefMetric1.meter("get").enabled(true);
        apefMetric1.histogram("get").enabled(true);

        verifyMetric(apefMetric1, 1);
    }

    private void verifyMetric(ApefMetric apefMetric, int count) {
        MetricItemCounter counter = apefMetric.counter("get");

        counter.inc();
        assertEquals(counter.count(), count);
        MetricItemTimer timer = apefMetric.timer("get");
        Timer.Context context = timer.start();
        if (context != null)
            context.stop();
        assertEquals(timer.count(), count);
        MetricItemMeter meter = apefMetric.meter("get");
        meter.mark();
        assertEquals(meter.count(), count);
        MetricItemHistogram histogram = apefMetric.histogram("get");
        histogram.update(1);
        assertEquals(histogram.count(), count);
    }

    @Test
    public void testGuagaMetric() {
        ApefMetrics metrics = ApefMetricsFactory.defaultMetrics();
        Cache cache = CacheBuilder.newBuilder()
                .recordStats()
                .maximumSize(100).build();
        metrics.registerGuava("TestCache", cache);

        Blocker blocker = new Blocker();

        for (int i = 0; i < 100; i++) {
            cache.put("key_" + i, "Value_" + i);
            if (i % 10 == 0) {
                metrics.cacheMetric().report();
            }
            cache.getIfPresent("key_" + i);
            cache.getIfPresent("key_");
        }

        blocker.end();

        blocker.awaitEnd();
    }
}