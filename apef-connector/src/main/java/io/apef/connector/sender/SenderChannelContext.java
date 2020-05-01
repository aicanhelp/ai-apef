package io.apef.connector.sender;

import io.apef.core.channel.MessageType;
import io.apef.base.cache.CacheStats;
import io.apef.base.utils.KeyMapper;
import io.apef.connector.base.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class SenderChannelContext<C extends ConnectorRequestContext<C>, K, T, R> implements ConnectorChannelContext {
    @Wither
    private MessageType requestType;
    @Wither
    private RequestEncoder<C, T> requestEncoder;
    @Wither
    private ResponseDecoder<C, T, R> responseDecoder;
    @Wither
    private KeyMapper<K, T> requestKeyMapper;
    @Wither
    private KeyMapper<K, R> responseKeyMapper;
    @Wither
    private KeyMapper<ByteBuf, T> affinityKeyMapper;
    @Wither
    private ExpireChecker<R> expireChecker;
    @Wither
    private SenderChannelCache<K, R> senderCache;

    private SenderChannel<C> senderChannel;

    SenderChannelContext(SenderChannel<C> senderChannel) {
        this.senderChannel = senderChannel;
    }

    public MessageType requestType() {
        return this.requestType;
    }

    public SenderChannel<C> endChannelContext() {
        check(this);

        this.senderChannel.registerChannelContext(this);
        return this.senderChannel;
    }

    R decodeResponse(C inContext, T request, ByteBuf byteBuf) throws Exception {
        return this.responseDecoder.decode(inContext, request, byteBuf);
    }

    ByteBuf encodeRequest(C outContext, T request) throws Exception {
        ByteBuf byteBuf = Unpooled.buffer();

        this.requestEncoder.encode(outContext, byteBuf, request);

        return byteBuf;
    }

    ByteBuf affinityKey(T requestContent) {
        return this.affinityKeyMapper.keyOf(requestContent);
    }

    public CacheStats cacheStats() {
        if (this.senderCache == null) return null;
        return this.senderCache.stats();
    }

    R getFromCacheByRequest(T request) {
        if (this.requestKeyMapper == null || this.senderCache == null) return null;
        Object key = this.requestKeyMapper.keyOf(request);
        if (key == null) return null;
        R result = this.senderCache.get(this.requestKeyMapper.keyOf(request));
        if (result != null && this.expireChecker.isExpired(result))
            return null;
        return result;
    }

    void putResponseToCache(R response) {
        if (this.responseKeyMapper == null || this.senderCache == null) return;
        Object key = this.responseKeyMapper.keyOf(response);
        if (key == null) return;
        this.senderCache.put(this.responseKeyMapper.keyOf(response), response);
    }
}
