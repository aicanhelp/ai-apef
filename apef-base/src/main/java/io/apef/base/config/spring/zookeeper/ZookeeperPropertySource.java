package io.apef.base.config.spring.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;
import org.springframework.util.ReflectionUtils;

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class ZookeeperPropertySource extends AbstractZookeeperPropertySource {

    private Map<String, String> properties = new LinkedHashMap<>();

    public ZookeeperPropertySource(String context, CuratorFramework source) {
        super(context, source);
        findProperties(this.getContext(), null);
    }

    @Override
    public Object getProperty(String name) {
        return this.properties.get(name);
    }

    private byte[] getPropertyBytes(String fullPath) {
        try {
            byte[] bytes = null;
            try {
                bytes = this.getSource().getData().forPath(fullPath);
            } catch (KeeperException e) {
                if (e.code() != KeeperException.Code.NONODE) { // not found
                    throw e;
                }
            }
            return bytes;
        } catch (Exception exception) {
            ReflectionUtils.rethrowRuntimeException(exception);
        }
        return null;
    }

    @Override
    public String[] getPropertyNames() {
        Set<String> strings = this.properties.keySet();
        return strings.toArray(new String[strings.size()]);
    }

    private void findProperties(String path, List<String> children) {
        try {
            log.trace("entering findProperties for path: " + path);
            if (children == null) {
                children = getChildren(path);
            }
            if (children == null || children.isEmpty()) {
                return;
            }
            for (String child : children) {
                String childPath = path + "/" + child;
                List<String> childPathChildren = getChildren(childPath);

                byte[] bytes = getPropertyBytes(childPath);
                if (bytes == null || bytes.length == 0) {
                    if (childPathChildren == null || childPathChildren.isEmpty()) {
                        registerKeyValue(childPath, "");
                    }
                } else {
                    registerKeyValue(childPath, new String(bytes, Charset.forName("UTF-8")));
                }

                // Check children even if we have found a value for the current znode
                findProperties(childPath, childPathChildren);
            }
            log.trace("leaving findProperties for path: " + path);
        } catch (Exception exception) {
            ReflectionUtils.rethrowRuntimeException(exception);
        }
    }

    private void registerKeyValue(String path, String value) {
        String key = sanitizeKey(path);
        this.properties.put(key, value);
    }

    private List<String> getChildren(String path) throws Exception {
        List<String> children = null;
        try {
            children = this.getSource().getChildren().forPath(path);
        } catch (KeeperException e) {
            if (e.code() != KeeperException.Code.NONODE) { // not found
                throw e;
            }
        }
        return children;
    }
}
