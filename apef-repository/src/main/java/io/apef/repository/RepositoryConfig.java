package io.apef.repository;

import io.apef.core.channel.ChannelConfig;
import lombok.Data;
import lombok.experimental.Accessors;
import io.apef.base.utils.KeyMapper;

@Data
@Accessors(chain = true)
public class RepositoryConfig extends ChannelConfig {
    private final static String DEFAULT_NAME = "Repository";
    private String name = "Repository";
    private boolean enableCache = false;
    private int maxCachedSize = 1024;
    private boolean cacheOnly = false;
    private boolean enableCacheMetrics = false;
    private KeyMapper keyMapper = request -> request;

    public RepositoryConfig validate() {
        if (!this.isNameChanged()) {
            throw new IllegalArgumentException("A unique name is required for the Repository");
        }
        return this;
    }

    public boolean isNameChanged() {
        return !DEFAULT_NAME.equals(this.name);
    }
}
