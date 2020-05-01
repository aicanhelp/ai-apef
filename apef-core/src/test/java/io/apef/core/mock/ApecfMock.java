package io.apef.core.mock;

import io.apef.core.channel.impl.ChannelMessageImpl;
import io.apef.core.channel.pipe.ChannelPipe;
import io.apef.core.channel.request.ChannelRequest;
import io.apef.core.channel.request.ChannelTxRequest;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.mockito.Mockito.spy;

public class ApecfMock<T, R, K> {
    private Function<T, K> keySupplier;
    private Map<K, Mock<K, R>> mockMap = new ConcurrentHashMap<>();
    private List<Mock<K, R>> mocks = new CopyOnWriteArrayList<>();

    public ApecfMock() {

    }

    public ApecfMock(Function<T, K> keySupplier) {
        this.keySupplier = keySupplier;
    }

    public Mock of(ChannelRequest request) {
        K key = this.key(request);
        Mock mock = mockMap.get(key);
        if (mock == null) {
            for (Mock<K, R> m : mocks) {
                if (m.filter.filter(key)) {
                    mock = m;
                    break;
                }
            }
        }
        if (mock == null) {
            throw new IllegalArgumentException("Mock not Found for key: " + key);
        }
        mock.inc();
        return mock;
    }

    private K key(ChannelRequest request) {
        if (this.keySupplier == null) return (K) ((ChannelMessageImpl) request).requestContent();
        return this.keySupplier.apply((T) ((ChannelMessageImpl) request).requestContent());
    }

    public ApecfMock.Mock<K, R> mock(K key) {
        return new Mock<>(this, key);
    }

    public ApecfMock.Mock<K, R> mock() {
        return new Mock<>(this);
    }

    /**
     * Mock a response with a specified request key
     *
     * @param key
     * @param response
     * @return
     */
    public ApecfMock<T, R, K> mock(K key, R response) {
        new Mock<>(this, key).response(response).endMock();
        return this;
    }

    /**
     * Mock a exception response with a specified request key
     *
     * @param key
     * @param e
     * @return
     */
    public ApecfMock<T, R, K> mock(K key, Exception e) {
        new Mock<>(this, key).exception(e).endMock();
        return this;
    }

    public static <C extends ChannelPipe> ChannelPipeMock<C> mockChannelPipe() {
        return ChannelPipeMock.mock();
    }

    public <CR extends ChannelTxRequest> CR mockRequest(CR request) {
        return ApecfRequestMock.from(request)
                .resultSupplier(ApecfMock.this::of)
                .endMock();
    }

    public int requestTimes(K key) {
        return this.mockMap.get(key).requestTimes.get();
    }

    public static class Mock<K, R> extends ApecfRequestMock.ResultMock<R, Mock<K, R>> {
        private ApecfMock<?, R, K> apecfMock;
        private K key;
        @Setter
        @Accessors(fluent = true)
        private Filter<K> filter;

        private AtomicInteger requestTimes = new AtomicInteger();

        private Mock(ApecfMock<?, R, K> apecfMock) {
            this.apecfMock = apecfMock;
        }

        private Mock(ApecfMock<?, R, K> apecfMock, K key) {
            this.apecfMock = apecfMock;
            this.key = key;
        }

        void inc() {
            this.requestTimes.incrementAndGet();
        }

        public ApecfMock<?, R, K> endMock() {
            if (this.key == null && this.filter == null) {
                throw new IllegalArgumentException("key and filter can not be null at the same time");
            }

            if (this.key != null && this.filter != null) {
                throw new IllegalArgumentException("key and filter can not be set at the same time");
            }

            if (this.key != null) {
                if (apecfMock.mockMap.containsKey(this.key)) {
                    throw new IllegalArgumentException("Mock has already set for key: " + this.key);
                }

                apecfMock.mockMap.put(this.key, this);
            } else {
                apecfMock.mocks.add(this);
            }

            return this.apecfMock;
        }
    }

    public interface Filter<T> {
        boolean filter(T key);
    }

}
