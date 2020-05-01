package io.apef.connector.sender;

import io.apef.core.channel.ChannelConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SenderChannelConfig extends ChannelConfig {
}
