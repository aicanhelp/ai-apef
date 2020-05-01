package io.apef.core.example;

import io.apef.core.APEF;
import io.apef.core.channel.*;
import io.apef.core.channel.pipe.B2CPipe;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;


public class SimpleExample {
    @Getter
    @Accessors(fluent = true)
    @AllArgsConstructor
    enum ExmapleType implements MessageType<ExmapleType> {
        Get((byte)0),
        Save((byte)1);
        private byte id;
    }

    BusinessChannel<?> businessChannel =
            APEF.createBusinessChannel(new ChannelConfig().setName("Business"))
                    .handler(ExmapleType.Get, (messageContext, requestContent) ->
                            messageContext.succeed("OK"))
                    .handler(ExmapleType.Save, (messageContext, requestContent) ->
                            messageContext.succeed("OK"))
                    .start();

    ClientChannel<?> clientChannel = APEF.createClientChannel(new ChannelConfig().setName("server"));
    B2CPipe<?, ?> pipe = clientChannel.B2CPipe(businessChannel);

    public void get() {
        pipe.request().messageType(ExmapleType.Get).requestContent("requestData")
                .retry(3, 3000).timeout(3000).idem("a")
                .end();
    }
}
