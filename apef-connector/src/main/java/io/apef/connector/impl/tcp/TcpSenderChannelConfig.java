package io.apef.connector.impl.tcp;

import io.apef.connector.sender.SenderChannelConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.concurrent.TimeUnit;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class TcpSenderChannelConfig extends SenderChannelConfig {
    private String name = "TcpSenderChannel";
    private String tcpClientName;
    private int timeout = 3000;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
}
