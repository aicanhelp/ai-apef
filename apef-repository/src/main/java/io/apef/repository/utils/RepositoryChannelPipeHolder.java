package io.apef.repository.utils;

import io.apef.repository.channel.RepositoryChannelPipe;

/**
 * Work in one thread, threadSafe is not needed.
 *
 * @param <T>
 * @param <R>
 */
public abstract class RepositoryChannelPipeHolder<T, R> {
    private RepositoryChannelPipe<T, R> instance;

    protected RepositoryChannelPipeHolder() {
    }

    public RepositoryChannelPipe<T, R> RepositoryChannelPipe() {
        if (instance != null) return instance;
        this.instance = this.create();
        return this.instance;
    }

    protected abstract RepositoryChannelPipe<T, R> create();
}
