package io.apef.base.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Use for define the error code item as the static fields in one class
 */
@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class StaticErrorCode implements ErrorCode {
    private ErrSeverity severity;
    private String appCode, errCode, message;
}
