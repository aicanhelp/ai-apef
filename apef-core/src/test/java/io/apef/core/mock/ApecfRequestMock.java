package io.apef.core.mock;

import io.apef.core.channel.impl.ChannelMessageImpl;
import io.apef.core.channel.request.ChannelTxRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

public class ApecfRequestMock {

    public static <T extends ChannelTxRequest, R> RequestMock<T, R> from(T request) {
        return new RequestMock<>(request);
    }

    @Slf4j
    public static class RequestMock<T extends ChannelTxRequest, R> {
        @Getter(AccessLevel.PROTECTED)
        @Accessors(fluent = true)
        private T request;

        @Setter(AccessLevel.PROTECTED)
        @Accessors(fluent = true)
        private Function<T, ResultMock<R, ?>> resultSupplier;

        private RequestMock(T request) {
            this.request = request;
        }

        public T endMock() {
            ChannelMessageImpl spiedRequest = spy((ChannelMessageImpl) request);

            doAnswer(invocation -> {
                ResultMock<R, ?> resultMock = resultSupplier.apply((T) spiedRequest);

                if (resultMock.exception() != null) {
                    spiedRequest.responseFuture().complete(resultMock.exception().getMessage(),
                            resultMock.exception());
                } else
                    spiedRequest.responseFuture().complete(resultMock.response());

                spiedRequest.responseFuture().reset();
                return null;
            }).when(spiedRequest).end();

            doAnswer(invocation -> {
                ResultMock<R, ?> resultMock = resultSupplier.apply((T) spiedRequest);
                if (resultMock.exception() != null) {
                    spiedRequest.responseFuture().complete(resultMock.exception().getMessage(),
                            resultMock.exception());
                } else
                    spiedRequest.responseFuture().complete(resultMock.response());

                // spiedRequest.responseFuture().reset();
                return spiedRequest.responseFuture();
            }).when(spiedRequest).future();

            return (T) spiedRequest;
        }
    }

    @Getter
    @Accessors(fluent = true)
    public static class ResultMock<R, T extends ResultMock<R, T>> {
        private R response;
        private Exception exception;

        public T response(R response) {
            this.response = response;
            return (T) this;
        }

        public T exception(Exception e) {
            this.exception = e;
            return (T) this;
        }
    }
}
