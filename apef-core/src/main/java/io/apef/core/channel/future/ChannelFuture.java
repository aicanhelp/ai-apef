package io.apef.core.channel.future;

import lombok.extern.slf4j.Slf4j;

public interface ChannelFuture<O> {

    static <O> ChannelFuture<O> completeFuture(boolean success) {
        ChannelFutureImpl<O> future = new ChannelFutureImpl<>();
        future.complete(success);
        return future;
    }

    static <O> ChannelFuture<O> completeFuture(String errMsg, Throwable cause) {
        ChannelFutureImpl<O> future = new ChannelFutureImpl<>();
        future.complete(errMsg, cause);
        return future;
    }

    static <O> ChannelFuture<O> completeFuture(O value) {
        ChannelFutureImpl<O> future = new ChannelFutureImpl<>();
        future.complete(value);
        return future;
    }

    static <O> ChannelCompletableFuture<O> completableFuture() {
        return new ChannelFutureImpl<>();
    }

    ChannelFuture<O> onSuccess(SuccessHandler<O> successHandler);

    ChannelFuture<O> onFailure(FailureHandler failureHandler);

    @Slf4j
    class SuccessHandlerChain<O> {
        private SuccessHandlerChain<O> top;
        private SuccessHandler<O> handler;
        private SuccessHandlerChain<O> next;

        SuccessHandlerChain(SuccessHandler<O> handler) {
            this.handler = handler;
            this.top = this;
        }

        public SuccessHandlerChain<O> next(SuccessHandler<O> handler) {
            this.next = new SuccessHandlerChain<>(handler);
            this.next.top = this.top;
            return this.next;
        }

        void complete(O value) {
            this.top.doComplete(value);
        }

        private void doComplete(O value) {
            try {
                this.handler.handle(value);
            } catch (Exception ex) {
                throw new RuntimeException("Exception thrown on handling Successful result:", ex);
            }
            if (this.next != null)
                this.next.doComplete(value);
        }
    }

    @Slf4j
    class FailureHandlerChain {
        private FailureHandlerChain top;
        private FailureHandler handler;
        private FailureHandlerChain next;

        FailureHandlerChain(FailureHandler handler) {
            this.handler = handler;
            this.top = this;
        }

        public FailureHandlerChain next(FailureHandler handler) {
            this.next = new FailureHandlerChain(handler);
            this.next.top = this.top;
            return this.next;
        }

        void complete(String errMsg, Throwable cause) {
            this.top.doComplete(errMsg, cause);
        }

        void doComplete(String errMsg, Throwable cause) {
            try {
                this.handler.handle(errMsg, cause);
            } catch (Exception ex) {
                throw new RuntimeException("Exception thrown on handling failure result:", ex);
            }

            if (this.next != null)
                this.next.doComplete(errMsg, cause);
        }
    }

}
