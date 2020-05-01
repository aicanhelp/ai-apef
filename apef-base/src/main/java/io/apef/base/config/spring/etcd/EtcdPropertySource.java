package io.apef.base.config.spring.etcd;

import lombok.extern.slf4j.Slf4j;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class EtcdPropertySource extends EnumerablePropertySource<EtcdClient> {

    private final Map<String, String> properties;
    private final String prefix;
    private final EtcdConfigProperties config;

    public EtcdPropertySource(String root, EtcdClient source, EtcdConfigProperties config) {
        super(root, source);
        this.properties = new HashMap<>();
        this.prefix = root.startsWith(EtcdConstants.PATH_SEPARATOR) ? root
                + EtcdConstants.PATH_SEPARATOR : EtcdConstants.PATH_SEPARATOR + root
                + EtcdConstants.PATH_SEPARATOR;
        this.config = config;
    }

    public void init() {
        try {
            final EtcdKeysResponse response = getSource().getDir(getName()).recursive()
                    .timeout(config.getTimeout(), config.getTimeoutUnit()).send().get();

            if (response.node != null) {
                process(response.node);
            }
        } catch (EtcdException e) {
            if (e.errorCode == 100) {//key not found, no need to print stack trace
                log.warn("Unable to init property source: " + getName() + ", " + e.getMessage());
            } else {
                log.warn("Unable to init property source: " + getName(), e);
            }
        } catch (Exception e) {
            log.warn("Unable to init property source: " + getName(), e);

        }
    }

    @Override
    public String[] getPropertyNames() {
        return properties.keySet().toArray(new String[0]);
    }

    @Override
    public Object getProperty(String name) {
        return properties.get(name);
    }

    private void process(final EtcdKeysResponse.EtcdNode root) {
        if (!StringUtils.isEmpty(root.value)) {
            final String key = root.key.substring(this.prefix.length());

            properties.put(key.replace(EtcdConstants.PATH_SEPARATOR,
                    EtcdConstants.PROPERTIES_SEPARATOR), root.value);
        }

        if (!CollectionUtils.isEmpty(root.nodes)) {
            for (EtcdKeysResponse.EtcdNode node : root.nodes) {
                process(node);
            }
        }
    }
}