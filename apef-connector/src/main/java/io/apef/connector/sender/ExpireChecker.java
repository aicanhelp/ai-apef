package io.apef.connector.sender;

public interface ExpireChecker<R> {
    boolean isExpired(R o);
}
