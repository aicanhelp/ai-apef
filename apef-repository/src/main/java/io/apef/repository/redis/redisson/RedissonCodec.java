package io.apef.repository.redis.redisson;


import io.netty.buffer.ByteBuf;

public interface RedissonCodec<K, V> {
    byte[] encodeKey(K key);

    K decodeKey(ByteBuf v);

    byte[] encodeValue(V v);

    V decodeValue(ByteBuf byteBuf);
}
