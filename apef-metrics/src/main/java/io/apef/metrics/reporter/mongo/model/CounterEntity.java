package io.apef.metrics.reporter.mongo.model;

public class CounterEntity extends BaseEntity {

    private Object count;

    public Object getCount() {
        return count;
    }

    public void setCount(final Object count) {
        this.count = count;
        put("count", count);
    }
}
