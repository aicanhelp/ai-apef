package io.apef.base.config.spring.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.zookeeper.KeeperException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import java.io.Closeable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type.*;

@Slf4j
public class ConfigWatcher implements Closeable, TreeCacheListener, ApplicationEventPublisherAware {

    private AtomicBoolean running = new AtomicBoolean(false);
    private List<String> contexts;
    private CuratorFramework source;
    private ApplicationEventPublisher publisher;
    private HashMap<String, TreeCache> caches;

    public ConfigWatcher(List<String> contexts, CuratorFramework source) {
        this.contexts = contexts;
        this.source = source;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void start() {
        if (this.running.compareAndSet(false, true)) {
            this.caches = new HashMap<>();
            for (String context : this.contexts) {
                if (!context.startsWith("/")) {
                    context = "/" + context;
                }
                try {
                    TreeCache cache = TreeCache.newBuilder(this.source, context).build();
                    cache.getListenable().addListener(this);
                    cache.start();
                    this.caches.put(context, cache);
                    // no race condition since ZookeeperAutoConfiguration.curatorFramework
                    // calls curator.blockUntilConnected
                } catch (KeeperException.NoNodeException e) {
                    // no node, ignore
                } catch (Exception e) {
                    log.error("Error initializing listener for context " + context, e);
                }
            }
        }
    }

    @Override
    public void close() {
        if (this.running.compareAndSet(true, false)) {
            for (TreeCache cache : this.caches.values()) {
                cache.close();
            }
            this.caches = null;
        }
    }


    public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
        TreeCacheEvent.Type eventType = event.getType();
        if (eventType == NODE_ADDED || eventType == NODE_REMOVED || eventType == NODE_UPDATED) {
            this.publisher.publishEvent(new RefreshEvent(this, event, getEventDesc(event)));
        }
    }

    public String getEventDesc(TreeCacheEvent event) {
        StringBuilder out = new StringBuilder();
        out.append("type=").append(event.getType());
        out.append(", path=").append(event.getData().getPath());
        byte[] data = event.getData().getData();
        if (data != null && data.length > 0) {
            out.append(", data=").append(new String(data, Charset.forName("UTF-8")));
        }
        return out.toString();
    }
}
