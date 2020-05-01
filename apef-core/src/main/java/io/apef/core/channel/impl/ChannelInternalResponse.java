package io.apef.core.channel.impl;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class ChannelInternalResponse<T, R>{
    boolean success=true;
    T requestContent;
    R responseContent;
    Throwable cause;
    String errMsg;
}
