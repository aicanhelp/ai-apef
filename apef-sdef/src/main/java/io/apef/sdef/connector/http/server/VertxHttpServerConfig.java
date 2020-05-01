package io.apef.sdef.connector.http.server;

import io.apef.base.utils.ObjectFormatter;
import io.apef.base.config.factory.IConfigBase;
import io.apef.base.config.factory.XFactoryConfig;
import io.vertx.core.http.HttpServerOptions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.Min;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class VertxHttpServerConfig extends HttpServerOptions implements IConfigBase {
    private String name = "HttpConnector";
    @Min(100)
    private long timeout = 6000;
    private boolean enabled;
    private CorsConfig cors;

    @Override
    public String toString() {
        return ObjectFormatter.toString(this);
    }

    public static class VertxServerFactoryConfig extends XFactoryConfig<VertxHttpServerConfig> {

    }
}
