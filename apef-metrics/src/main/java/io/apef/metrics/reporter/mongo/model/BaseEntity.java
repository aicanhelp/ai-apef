package io.apef.metrics.reporter.mongo.model;

import java.util.Date;
import java.util.HashMap;

public abstract class BaseEntity extends HashMap<String,Object> {

    public void setName(String name) {
        put("name", name);
    }

    public void setTimestamp(Date date) {
        put("timestamp", date);
    }
}
