package io.apef.metrics.reporter.mongo.model;

public class GaugeEntity extends BaseEntity {

    private Object value;

    public Object getValue() {
        return value;
    }

    public void setValue(final Object value) {
        this.value = value;
        put("value", value);
    }
}