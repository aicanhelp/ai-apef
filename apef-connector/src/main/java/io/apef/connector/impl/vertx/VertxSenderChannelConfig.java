package io.apef.connector.impl.vertx;

import io.apef.connector.sender.SenderChannelConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class VertxSenderChannelConfig extends SenderChannelConfig {
    private String name = "VertxSenderChannel";
    private String serviceAddress;
    private boolean post = true;
    private String vertxClient;
}
