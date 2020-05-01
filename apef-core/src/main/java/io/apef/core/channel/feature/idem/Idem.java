package io.apef.core.channel.feature.idem;

import io.apef.core.channel.feature.ChannelMessageFeature;
import io.apef.core.channel.message.ChannelInternalRequestMessage;
import io.apef.core.channel.message.MessageStatus;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class Idem extends ChannelMessageFeature {
    @Getter
    @Accessors(fluent = true)
    private Object key;
    @Getter
    @Setter
    @Accessors(fluent = true)
    private boolean primary = true;

    //todo: consider using other data structure
    private List<ChannelInternalRequestMessage> followers;

    public Idem(Object key, ChannelInternalRequestMessage channelMessage) {
        super(channelMessage);
        Preconditions.checkNotNull(key, "Idem key can not be null");
        this.key = key;
    }

    public void addFollower(ChannelInternalRequestMessage message) {
        if (this.channelMessage().status() == MessageStatus.Finished) return;
        if (followers == null) followers = new ArrayList<>();
        message.features().idem().primary(false);
        followers.add(message);
    }

    public boolean followBy(ChannelInternalRequestMessage message) {
        if (!primary) return false;
        if (message.features().idem() == null) return false;
        if (message.features().idem().primary()) return false;
        return followers.contains(message);
    }

    public void doOnFollowers(Consumer<ChannelInternalRequestMessage> action) {
        if (followers == null) return;
        followers.forEach(action);
    }

    public void finishFollowers() {
        doOnFollowers(message -> {
            message.finishByFollow(channelMessage());
        });
    }
}
