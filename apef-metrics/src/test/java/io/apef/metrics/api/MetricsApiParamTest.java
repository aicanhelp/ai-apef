package io.apef.metrics.api;

import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;

import static org.testng.Assert.*;


public class MetricsApiParamTest extends BaseUnitSpec {
    @Test
    public void testPathWithoutRoot() {
        assertEquals(MetricsApiParam.item.pathContext(),
                ":" + MetricsApiParam.action.name()
                        + "/:" + MetricsApiParam.metric.name()
                        + "/:" + MetricsApiParam.type.name()
                        + "/:" + MetricsApiParam.item.name());
    }

    @Test
    public void testPathWithRoot() {
        String root = "/";
        assertEquals(MetricsApiParam.item.pathContext(root),
                root + ":" + MetricsApiParam.action.name()
                        + "/:" + MetricsApiParam.metric.name()
                        + "/:" + MetricsApiParam.type.name()
                        + "/:" + MetricsApiParam.item.name());
        root = "test";
        assertEquals(MetricsApiParam.item.pathContext(root),
                root + "/:" + MetricsApiParam.action.name()
                        + "/:" + MetricsApiParam.metric.name()
                        + "/:" + MetricsApiParam.type.name()
                        + "/:" + MetricsApiParam.item.name());
    }
}