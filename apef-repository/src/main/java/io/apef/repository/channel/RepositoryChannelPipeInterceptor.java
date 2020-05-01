package io.apef.repository.channel;

import io.apef.core.channel.impl.FromBInterceptorImpl;
import io.apef.repository.message.*;
import io.apef.repository.RepositoryRequestType;
import lombok.AccessLevel;
import lombok.Setter;

import java.util.Map;

public class RepositoryChannelPipeInterceptor<K, V> extends FromBInterceptorImpl {
    private final static Throwable NULL_POINT_EXCEPTION = new NullPointerException();
    private RepositoryCache<K, V> repositoryCache;
    @Setter(AccessLevel.PROTECTED)
    private boolean cacheOnly;

    public RepositoryChannelPipeInterceptor(RepositoryCache<K, V> repositoryCache, boolean cacheOnly) {
        this.repositoryCache = repositoryCache;
        this.cacheOnly = cacheOnly;
        if (cacheOnly && repositoryCache == null) {
            throw new IllegalArgumentException("RepositoryCache must be set for cacheOnly.");
        }
        super.beforeSendRequest(RepositoryRequestType.GET, this::beforeGet)
                .beforeHandleResponse(RepositoryRequestType.GET, this::afterGet)
                .beforeSendRequest(RepositoryRequestType.EXISTS, this::beforeExists)
                .beforeHandleResponse(RepositoryRequestType.EXISTS, this::afterExists)
                .beforeSendRequest(RepositoryRequestType.SAVE, this::beforeSave)
                .beforeSendRequest(RepositoryRequestType.GET_ALL, this::beforeGetAll)
                .beforeHandleResponse(RepositoryRequestType.GET_ALL, this::afterGetAll)
                .beforeSendRequest(RepositoryRequestType.PUT_ALL, this::handlePutAll);
    }

    private void beforeGet(RepositoryGetMessage<K, V> message) {
        K key = message.requestContent();
        if (key == null) {
            message.messageContext().fail("Key can not be null for Get Operation", NULL_POINT_EXCEPTION);
            return;
        }
        if (repositoryCache != null) {
            V result = repositoryCache.get(key);

            if (result != null) {
                message.hitCache(true);
                message.messageContext().succeed(result);
                message.finish();
            } else if (!message.readThrough() || cacheOnly) {
                message.messageContext().succeed(result);
                message.finish();
            }
        }
    }

    private void afterGet(RepositoryGetMessage<K, V> message) {
        if (message.response().responseContent() != null &&
                !message.hitCache() && repositoryCache != null) {
            repositoryCache.save(message.response().responseContent());
        }
    }

    private void beforeSave(RepositorySaveMessage<V> message) {
        if (message.requestContent() == null) {
            message.messageContext().fail("Value can not be null for Save Operation", NULL_POINT_EXCEPTION);
        }
        if (this.repositoryCache != null) {
            this.repositoryCache.save(message.requestContent());
        }
        if (cacheOnly) {
            message.messageContext().succeed(true);
            message.finish();
        }
    }

    private void beforeExists(RepositoryExistsMessage<K, V> message) {
        K key = message.requestContent();
        if (key == null) {
            message.messageContext().fail("Key can not be null for Exist Operation", NULL_POINT_EXCEPTION);
        }
        if (repositoryCache != null) {
            boolean result = repositoryCache.exists(key);
            if (result) {
                message.hitCache(true);
                message.messageContext().succeed(true);
                message.finish();
            } else if (!message.readThrough() || cacheOnly) {
                message.messageContext().succeed(false);
                message.finish();
            }
        }
    }

    private void afterExists(RepositoryExistsMessage<K, V> message) {
        if (!message.hitCache() && message.value() != null && repositoryCache != null) {
            repositoryCache.save(message.value());
        }
    }

    private void beforeGetAll(RepositoryGetAllMessage<K, V> message) {
        if (message.requestContent() == null || message.requestContent().isEmpty()) {
            message.messageContext().fail("Keys can not be empty for GetAll Operation", NULL_POINT_EXCEPTION);
        }
        if (repositoryCache != null) {
            Map<K, V> cachedValues = this.repositoryCache.getAll(message.keys());
            if (!message.readThrough() || cacheOnly) {
                message.messageContext().succeed(cachedValues);
                message.finish();
            } else if (cachedValues != null && !cachedValues.isEmpty()) {
                if (message.keys().size() == cachedValues.size()) {
                    message.messageContext().succeed(cachedValues);
                    message.finish();
                } else {
                    message.cachedValues(cachedValues);
                    message.keys().removeAll(cachedValues.keySet());
                }
            }
        }
    }

    private void afterGetAll(RepositoryGetAllMessage<K, V> message) {
        if (repositoryCache != null) {
            if (message.cachedValues() != null) {
                if (message.response().responseContent() == null) {
                    message.response().responseContent(message.cachedValues());
                } else
                    message.response().responseContent().putAll(message.cachedValues());
            }
        }
    }

    private void handlePutAll(RepositoryPutAllMessage<K, V> message) {
        if (message.requestContent() == null || message.requestContent().isEmpty()) {
            message.messageContext().fail("Values can not be empty for PutAll Operation", NULL_POINT_EXCEPTION);
        }
        if (repositoryCache != null) {
            repositoryCache.putAll(message.values());
            if (cacheOnly) {
                message.messageContext().succeed(true);
                message.finish();
            }
        }
    }
}
