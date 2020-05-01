package io.apef.connector.sender;

import io.apef.core.mock.ApecfMock;

import java.util.function.Function;

/**
 * Mock a sender
 *
 * @param <T>
 * @param <R>
 * @param <K>
 */
public class MockSender<T, R, K> {
    private ApecfMock<T, R, K> mock;

    public MockSender(Function<T, K> keySupplier) {
        this.mock = new ApecfMock<>(keySupplier);
    }

    /**
     * Mock a SenderRequest
     *
     * @return
     */
    public SenderRequest<?, T, R> mockRequest() {
        return mock.mockRequest(SenderRequest.newRequest());
    }

    /**
     * Create a mock for a transaction
     *
     * @return
     */
    public ApecfMock.Mock<K, R> mock() {
        return this.mock.mock();
    }

    /**
     * Create a mock for a transaction
     *
     * @param key
     * @return
     */
    public ApecfMock.Mock<K, R> mock(K key) {
        return this.mock.mock(key);
    }

    /**
     * Mock a response for a request key
     *
     * @param key
     * @param response
     * @return
     */
    public ApecfMock<T, R, K> mock(K key, R response) {
        return this.mock.mock(key, response);
    }

    /**
     * Mock a exception response for a request key
     *
     * @param key
     * @param e
     * @return
     */
    public ApecfMock<T, R, K> mock(K key, Exception e) {
        return this.mock.mock(key, e);
    }
}
