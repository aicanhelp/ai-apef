package io.apef.base.exception;

import io.apef.base.utils.ObjectFormatter;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(chain = true)
public class ExceptionsConfiguration {
    private AppExCodeConfig global = new AppExCodeConfig();
    private Map<String, AppExCodeConfig> applications = new HashMap<>();

    public String toString() {
        return ObjectFormatter.toString(this);
    }
}
