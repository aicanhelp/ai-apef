package io.apef.connector.impl.vertx;

import io.apef.connector.acceptor.AcceptorChannelConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class VertxAcceptorChannelConfig extends AcceptorChannelConfig{
    private String name = "VertxAcceptor";
    private String httpServer = "default";
    private String context = "/";
    private boolean postOnly = false;
}
