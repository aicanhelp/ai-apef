package io.apef.sdef.connector.http.server;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.impl.BodyHandlerImpl;


public class SimpleBodyHandler implements BodyHandler {
    private static final Logger log = LoggerFactory.getLogger(BodyHandlerImpl.class);

    private static final String BODY_HANDLED = "__body-handled";

    @Override
    public void handle(RoutingContext context) {
        HttpServerRequest request = context.request();
        // we need to keep state since we can be called again on reroute
        Boolean handled = context.get(BODY_HANDLED);
        if (handled == null || !handled) {
            BHandler handler = new BHandler(context);
            request.handler(handler);
            request.endHandler(v -> handler.end());
            context.put(BODY_HANDLED, true);
        } else {
            context.next();
        }
    }

    @Override
    public BodyHandler setBodyLimit(long bodyLimit) {
        return this;
    }

    @Override
    public BodyHandler setUploadsDirectory(String uploadsDirectory) {
        return null;
    }

    @Override
    public BodyHandler setMergeFormAttributes(boolean mergeFormAttributes) {
        return this;
    }

    @Override
    public BodyHandler setDeleteUploadedFilesOnEnd(boolean deleteUploadedFilesOnEnd) {
        return this;
    }

    private class BHandler implements Handler<Buffer> {

        RoutingContext context;
        Buffer body = Buffer.buffer();

        public BHandler(RoutingContext context) {
            this.context = context;
            context.request().exceptionHandler(context::fail);
        }


        @Override
        public void handle(Buffer buff) {
            body.appendBuffer(buff);
        }

        void end() {
            context.setBody(body);
            context.next();
        }

    }
}
