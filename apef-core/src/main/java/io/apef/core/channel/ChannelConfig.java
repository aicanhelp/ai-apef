package io.apef.core.channel;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.TreeMap;

@Data
@Accessors(chain = true)
public class ChannelConfig<T extends MessageType> {
    private String name = "Channel";
    private int queueSize = 16384;

    private TreeMap<T, Integer> maxConcurrency = new TreeMap<>();

    public int[] maxConcurrencies() {
        if (maxConcurrency == null) return null;
        int len = maxConcurrency.lastKey().id();
        int[] maxConcurrencies = new int[len];
        maxConcurrency.forEach((messageType, value) -> {
            maxConcurrencies[messageType.id()] = value;
        });
        return maxConcurrencies;
    }

}
