package io.apef.connector.acceptor;

/**
 * @param <IC> inStream Context
 * @param <OC> outStream Context
 * @param <T>  Request
 * @param <R>  Response
 */
public interface AcceptorHandler<IC, OC, T, R> {
    void handle(AcceptorRequestContext<IC, OC, T, R> context);
}
