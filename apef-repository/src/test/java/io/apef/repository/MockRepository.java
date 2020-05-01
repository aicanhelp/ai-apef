package io.apef.repository;

import io.apef.core.mock.ApecfMock;
import io.apef.core.mock.ApecfMockStubber;
import io.apef.core.mock.ChannelPipeMock;
import io.apef.core.channel.BusinessChannel;
import io.apef.base.cache.CacheStats;
import io.apef.repository.channel.RepositoryChannel;
import io.apef.repository.channel.RepositoryChannelPipe;
import io.apef.repository.channel.RepositoryChannelPipeImpl;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.Set;

@Getter
@Accessors(fluent = true)
public class MockRepository<K, V> implements Repository<K, V> {
    private ChannelPipeMock<RepositoryChannelPipe<K, V>> channelPipeMock;
    private ApecfMockStubber.ApecfMockStub<K, V> mockGet;
    private ApecfMockStubber.ApecfMockStub<V, Boolean> mockSave;
    private ApecfMockStubber.ApecfMockStub<K, Boolean> mockExist;
    private ApecfMockStubber.ApecfMockStub<Set<K>, Map<K, V>> mockGetAll;
    private ApecfMockStubber.ApecfMockStub<Map<K, V>, Boolean> mockPutAll;

    public MockRepository() {
        this.channelPipeMock = ApecfMock.mockChannelPipe();
        this.mockGet = this.channelPipeMock.mock(RepositoryRequestType.GET);
        this.mockSave = this.channelPipeMock.mock(RepositoryRequestType.SAVE);
        this.mockExist = this.channelPipeMock.mock(RepositoryRequestType.EXISTS);
        this.mockGetAll = this.channelPipeMock.mock(RepositoryRequestType.GET_ALL);
        this.mockPutAll = this.channelPipeMock.mock(RepositoryRequestType.PUT_ALL);
    }

    @Override
    public RepositoryChannelPipe<K, V> repositoryChannelPipe(BusinessChannel srcChannel) {
        return this.channelPipeMock.bind(new RepositoryChannelPipeImpl<>(srcChannel,
                new RepositoryChannel<>(new RepositoryConfig(), null, null)));
    }

    @Override
    public CacheStats cacheStats() {
        return null;
    }
}
