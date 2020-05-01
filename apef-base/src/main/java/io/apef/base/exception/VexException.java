package io.apef.base.exception;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class VexException extends Exception {
    private int errCode = -1;
    private int httpStatusCode = -1;

    protected VexException(int errCode, String defaultMsg) {
        super(defaultMsg);
        this.errCode = errCode;
        this.httpStatusCode = errCode;
    }

    protected VexException(int errCode, int httpStatusCode, String defaultMsg) {
        super(defaultMsg);
        this.errCode = errCode;
        this.httpStatusCode = httpStatusCode;
    }

    protected VexException(int errCode) {
        this.errCode = errCode;
    }

    protected VexException(String defaultMsg) {
        super(defaultMsg);
    }

    protected VexException(Throwable throwable) {
        super(throwable);
        this.errCode = VexExceptions.E_000.exception().errCode;
        this.httpStatusCode = VexExceptions.E_000.exception().httpStatusCode;
    }
}
