package io.apef.base.config.spring.etcd;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.concurrent.TimeUnit;

@Data
@Accessors(chain = true)
public class EtcdConfigProperties {
    private boolean enabled = true;
    private String prefix = "config";
    private String defaultContext = "application";
    private String profileSeparator = "-";
    private int timeout = 1;
    private TimeUnit timeoutUnit = TimeUnit.SECONDS;

    public EtcdConfigProperties() {
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EtcdConfigProperties that = (EtcdConfigProperties) o;

        if (enabled != that.enabled) return false;
        if (timeout != that.timeout) return false;
        if (prefix != null ? !prefix.equals(that.prefix) : that.prefix != null) return false;
        if (defaultContext != null ? !defaultContext.equals(that.defaultContext) : that.defaultContext != null)
            return false;
        if (profileSeparator != null ? !profileSeparator.equals(that.profileSeparator) : that.profileSeparator != null)
            return false;
        return timeoutUnit == that.timeoutUnit;
    }

    @Override
    public int hashCode() {
        int result = (enabled ? 1 : 0);
        result = 31 * result + (prefix != null ? prefix.hashCode() : 0);
        result = 31 * result + (defaultContext != null ? defaultContext.hashCode() : 0);
        result = 31 * result + (profileSeparator != null ? profileSeparator.hashCode() : 0);
        result = 31 * result + timeout;
        result = 31 * result + (timeoutUnit != null ? timeoutUnit.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("EtcdConfigProperties{enabled=%s, prefix='%s', defaultContext='%s', profileSeparator='%s', timeout=%d, timeoutUnit=%s}", enabled, prefix, defaultContext, profileSeparator, timeout, timeoutUnit);
    }
}
