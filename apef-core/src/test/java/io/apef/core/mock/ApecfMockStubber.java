package io.apef.core.mock;


import io.apef.core.channel.MessageType;
import io.apef.core.channel.impl.ChannelMessageImpl;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public class ApecfMockStubber {
    private Map<MessageType, List<ApecfMockStub>> stubListMap = new ConcurrentHashMap<>();

    private Consumer<MessageType> onMock;

    public <T, R> ApecfMockStub<T, R> mock(MessageType messageType) {
        this.stubListMap.putIfAbsent(messageType, new CopyOnWriteArrayList<>());
        if (this.onMock != null) this.onMock.accept(messageType);
        return new ApecfMockStub<>(messageType, this);
    }

    public void onMock(Consumer<MessageType> consumer) {
        this.onMock = consumer;
        this.stubListMap.keySet().forEach(messageType -> {
            consumer.accept(messageType);
        });
    }

    protected void handle(ChannelMessageImpl message) {
        List<ApecfMockStub> stubList = stubListMap.get(message.messageType());
        if (stubList == null) {
            throw new IllegalArgumentException("MockStub not found for request: " + message.requestContent());
        }
        for (ApecfMockStub mockStub : stubList) {
            if ((Boolean) mockStub.filter.apply(message.requestContent())) {
                if (mockStub.exception != null) {
                    message.fail(mockStub.exception().getMessage(), mockStub.exception());
                } else {
                    message.succeed(mockStub.response());
                }
                message.finish();
                return;
            }
        }
        throw new IllegalArgumentException("MockStub not found for request: " + message.requestContent());
    }

    private List<ApecfMockStub> stubList(MessageType messageType) {
        List<ApecfMockStub> stubList = stubListMap.get(messageType);
        if (stubList == null) {
            stubList = new CopyOnWriteArrayList<>();
        }
        this.stubListMap.put(messageType, stubList);
        return stubList;
    }

    public static class ApecfMockStub<T, R> {
        private ApecfMockStubber stubber;
        private MessageType messageType;

        @Getter(AccessLevel.PROTECTED)
        @Accessors(fluent = true)
        private Function<T, Boolean> filter;

        @Getter(AccessLevel.PROTECTED)
        @Accessors(fluent = true)
        private R response;

        @Getter(AccessLevel.PROTECTED)
        @Accessors(fluent = true)
        private Exception exception;

        private boolean ended;

        public ApecfMockStub(MessageType messageType, ApecfMockStubber stubber) {
            this.messageType = messageType;
            this.stubber = stubber;
        }

        public ApecfMockStub<T, R> exception(Exception exception) {
            if (!this.ended) {
                this.exception = exception;
                return this;
            }
            return new ApecfMockStub<T, R>(this.messageType, this.stubber).exception(exception);
        }

        public ApecfMockStub<T, R> response(R response) {
            if (!this.ended) {
                this.response = response;
                return this;
            }
            return new ApecfMockStub<T, R>(this.messageType, this.stubber).response(response);
        }

        public ApecfMockStub<T, R> filter(Function<T, Boolean> filter) {
            if (!this.ended) {
                this.filter = filter;
                return this;
            }
            return new ApecfMockStub<T, R>(this.messageType, this.stubber).filter(filter);
        }

        public ApecfMockStub<T, R> endMock() {
            stubber.stubList(this.messageType).add(this);
            this.ended = true;
            return new ApecfMockStub<>(this.messageType, this.stubber);
        }
    }
}
