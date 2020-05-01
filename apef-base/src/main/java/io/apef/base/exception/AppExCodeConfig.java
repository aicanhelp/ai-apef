package io.apef.base.exception;

import io.apef.base.utils.ObjectFormatter;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(chain = true)
public class AppExCodeConfig {
    private String appCode = "General";
    private String className;
    private Map<String, ExCodeConfig> errCodes = new HashMap<>();

    public String toString() {
        return ObjectFormatter.toString(this);
    }
}
