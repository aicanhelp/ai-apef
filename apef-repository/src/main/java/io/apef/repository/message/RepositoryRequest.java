package io.apef.repository.message;

import io.apef.core.channel.request.B2CRequest;


public interface RepositoryRequest<M extends RepositoryRequest<M, T, R>,
        T, R> extends B2CRequest<M, T, R> {
}

