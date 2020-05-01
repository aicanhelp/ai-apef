package io.apef.core.channel.feature.idem;

import io.apef.core.channel.message.ChannelInternalRequestMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class IdempotentHandler {

    protected Map<Object, ChannelInternalRequestMessage> channelMessages = new HashMap<>();

    public void handle(ChannelInternalRequestMessage channelMessage) {
        Idem idem = channelMessage.features().idem();
        if (idem == null) return;
        Object key = idem.key();
        ChannelInternalRequestMessage existingMessage = channelMessages.get(key);
        if (existingMessage == channelMessage) return;
        if (existingMessage == null) {
            channelMessages.put(key, channelMessage);
        } else {
            existingMessage.features().idem().addFollower(channelMessage);
        }
    }

    public void end(ChannelInternalRequestMessage channelMessage) {
        Idem idem = channelMessage.features().idem();
        if (idem == null || !idem.primary()) return;

        channelMessages.remove(idem.key());
    }
}
