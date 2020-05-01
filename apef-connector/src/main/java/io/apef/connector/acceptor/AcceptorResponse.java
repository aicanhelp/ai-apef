package io.apef.connector.acceptor;

import io.apef.base.exception.VexException;
import io.apef.base.exception.VexExceptions;
import io.apef.connector.base.ConnectorResponse;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Accessors(fluent = true)
public class AcceptorResponse<R> extends ConnectorResponse<R> {
    private AcceptorRequestContext<?, ?, ?, R> acceptorRequestContext;

    private boolean end;

    protected AcceptorResponse(AcceptorRequestContext<?, ?, ?, R> acceptorRequestContext) {
        super();
        this.acceptorRequestContext = acceptorRequestContext;
    }

    public AcceptorResponse fail(String errMsg, Throwable ex) {
        VexException exception= VexExceptions.of(ex);

        return this.fail(exception.errCode(), errMsg, exception);
    }

    public AcceptorResponse fail(int statusCode, String errMsg, Throwable ex) {
//        if(log.isDebugEnabled()){
//            log.debug("Failed by "+Thread.currentThread().getName());
//        }
        if (end) return this;
        this.success(false);
        this.ex(ex);
        this.statusCode(statusCode);
        this.errMsg(errMsg);
        this.acceptorRequestContext.writeResponse();
        this.end = true;
        return this;
    }

    public AcceptorResponse fail(Throwable ex) {
        this.fail(ex != null ? ex.getMessage() : "Unknown Error", ex);
        return this;
    }

    public AcceptorResponse<R> succeed(R response) {
        if (end) return this;
        this.success(true);
        this.response(response);
        this.acceptorRequestContext.writeResponse();
        this.end = true;
        return this;
    }

}
