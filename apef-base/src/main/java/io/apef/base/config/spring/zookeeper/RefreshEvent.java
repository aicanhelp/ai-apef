package io.apef.base.config.spring.zookeeper;

import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.springframework.context.ApplicationEvent;

public class RefreshEvent extends ApplicationEvent {
    public RefreshEvent(ConfigWatcher configWatcher,
                        TreeCacheEvent event, String eventDesc) {
        super(configWatcher);
    }
}
