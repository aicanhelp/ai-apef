package io.apef.base.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public enum TestErrorCode implements ErrorCode {
    TIMEOUT(ErrSeverity.MILD, "0", "10000", "time out"),
    Failure(ErrSeverity.MILD, "0", "10001", "Error2");

    private ErrSeverity severity;
    private String appCode, errCode, message;

    @Override
    public String toString() {
        return this.toString("");
    }
}
