package io.apef.repository.channel;

import io.apef.core.channel.BusinessChannel;
import io.apef.core.channel.ChannelConfig;
import io.apef.core.channel.ClientChannelImpl;
import io.apef.core.channel.message.ChannelMessageContext;
import io.apef.repository.message.RepositoryExistsMessage;
import io.apef.repository.RepositoryConfig;
import io.apef.repository.RepositoryRequestType;
import io.apef.repository.message.RepositoryGetAllMessage;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Getter
@Accessors(fluent = true)
public class RepositoryChannel<K, V> extends ClientChannelImpl<RepositoryChannel<K, V>> {
    private RepositoryChannelStore<K, V> repositoryStore;
    private RepositoryCache<K, V> repositoryCache;
    private RepositoryConfig repositoryConfig;

    public RepositoryChannel(RepositoryConfig channelConfig,
                             RepositoryCache<K, V> repositoryCache,
                             RepositoryChannelStore<K, V> repositoryStore) {
        super(checkChannelConfig(channelConfig));
        this.repositoryConfig = channelConfig;
        this.repositoryStore = repositoryStore;
        this.repositoryCache = repositoryCache;

        super.handler(RepositoryRequestType.GET, this::handleGet)
                .handler(RepositoryRequestType.EXISTS, this::handleExists)
                .handler(RepositoryRequestType.SAVE, this::handleSave)
                .handler(RepositoryRequestType.GET_ALL, this::handleGetAll)
                .handler(RepositoryRequestType.PUT_ALL, this::handlePutAll)
                .start();
    }

    private void handleGet(ChannelMessageContext<V> messageContext, K key) {
        this.repositoryStore.get(key, (returnValue, ex) -> {
            if (ex != null) {
                messageContext.fail(ex.getMessage(), ex);
            } else {
                messageContext.succeed(returnValue);
            }
        });
    }

    private void handleExists(RepositoryExistsMessage<K, V> message) {
        if (this.repositoryConfig.isEnableCache()) {
            //load the value from store, and let the interceptor to put the value into cache;
            this.repositoryStore.get(message.key(), (returnValue, ex) -> {
                if (ex != null) {
                    message.messageContext().fail(ex.getMessage(), ex);
                } else {
                    if (returnValue != null) {
                        message.value(returnValue);
                        message.messageContext().succeed(true);
                    } else {
                        message.messageContext().succeed(false);
                    }
                }
            });
        } else {
            this.repositoryStore.exists(message.key(), (returnValue, ex) -> {
                if (ex != null) {
                    message.messageContext().fail(ex.getMessage(), ex);
                } else {
                    message.messageContext().succeed(returnValue);
                }
            });
        }
    }

    private void handleSave(ChannelMessageContext<Boolean> messageContext, V value) {
        this.repositoryStore.save(value, (returnValue, ex) -> {
            if (ex != null) {
                messageContext.fail(ex.getMessage(), ex);
            } else {
                messageContext.succeed(returnValue);
            }
        });
    }

    private void handleGetAll(RepositoryGetAllMessage<K, V> message) {
        this.repositoryStore.getAll(message.keys(), (returnValue, ex) -> {
            if (ex != null) {
                message.messageContext().fail(ex.getMessage(), ex);
            } else {
                message.messageContext().succeed(returnValue);
            }
        });
    }

    private void handlePutAll(ChannelMessageContext<Boolean> messageContext, Map<K, V> values) {
        this.repositoryStore.putAll(values, (returnValue, ex) -> {
            if (ex != null) {
                messageContext.fail(ex.getMessage(), ex);
            } else {
                messageContext.succeed(returnValue);
            }
        });
    }

    private static ChannelConfig checkChannelConfig(ChannelConfig channelConfig) {
        if (StringUtils.isEmpty(channelConfig.getName())) {
            channelConfig.setName("RepositoryChannel");
        }
        return channelConfig;
    }

    public RepositoryChannelPipe<K, V> repositoryChannelPipe(BusinessChannel businessChannel) {
        return new RepositoryChannelPipeImpl<>(businessChannel, this,
                this.repositoryConfig.isCacheOnly());
    }
}
