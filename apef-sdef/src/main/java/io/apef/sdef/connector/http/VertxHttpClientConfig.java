package io.apef.sdef.connector.http;

import io.apef.base.utils.ObjectFormatter;
import io.apef.base.config.factory.IConfigBase;
import io.apef.base.config.factory.XFactoryConfig;
import io.vertx.core.http.HttpClientOptions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class VertxHttpClientConfig extends HttpClientOptions implements IConfigBase {
    private String name;
    private boolean enabled = true;

    @Override
    public String toString() {
        return ObjectFormatter.toString(this);
    }

    public static class VertxHttpClientFactoryConfig extends XFactoryConfig<VertxHttpClientConfig> {

    }
}
