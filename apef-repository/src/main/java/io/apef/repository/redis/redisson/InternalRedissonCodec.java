package io.apef.repository.redis.redisson;

import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

public class InternalRedissonCodec<K, V> implements Codec {

    private RedissonCodec<K, V> redissonCodec;
    private Encoder encoder;
    private Encoder mapKeyEncoder;
    private Decoder<V> decoder;
    private Decoder<K> mapKeyDecoder;

    public InternalRedissonCodec(RedissonCodec<K, V> redissonCodec) {
        this.redissonCodec = redissonCodec;
        this.encoder = in -> redissonCodec.encodeValue((V) in);
        this.decoder = (buf, state) -> redissonCodec.decodeValue(buf);

        this.mapKeyEncoder = in -> redissonCodec.encodeKey((K) in);
        this.mapKeyDecoder = (buf, state) -> redissonCodec.decodeKey(buf);
    }


    @Override
    public Decoder<Object> getMapValueDecoder() {
        return (Decoder<Object>) this.decoder;
    }

    @Override
    public Encoder getMapValueEncoder() {
        return this.encoder;
    }

    @Override
    public Decoder<Object> getMapKeyDecoder() {
        return (Decoder<Object>) this.mapKeyDecoder;
    }

    @Override
    public Encoder getMapKeyEncoder() {
        return this.mapKeyEncoder;
    }

    @Override
    public Decoder<Object> getValueDecoder() {
        return (Decoder<Object>) this.decoder;
    }

    @Override
    public Encoder getValueEncoder() {
        return this.encoder;
    }
}
