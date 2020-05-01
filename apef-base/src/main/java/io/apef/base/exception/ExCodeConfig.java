package io.apef.base.exception;

import io.apef.base.utils.ObjectFormatter;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ExCodeConfig {
    private String errCode;
    private String message;
    private ErrSeverity severity;

    public String toString() {
        return ObjectFormatter.toString(this);
    }
}
