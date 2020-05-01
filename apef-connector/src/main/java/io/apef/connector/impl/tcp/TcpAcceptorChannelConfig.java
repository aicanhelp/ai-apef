package io.apef.connector.impl.tcp;

import io.apef.connector.acceptor.AcceptorChannelConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class TcpAcceptorChannelConfig extends AcceptorChannelConfig {
    private String name = "TcpAcceptorChannel";
    private String tcpServer="default";
}
