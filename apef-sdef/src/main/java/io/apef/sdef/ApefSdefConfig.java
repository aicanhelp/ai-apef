package io.apef.sdef;

import io.apef.sdef.cluster.ApefClustersConfig;
import io.apef.sdef.connector.http.VertxHttpClientConfig;
import io.apef.sdef.connector.http.server.VertxHttpServerConfig;
import io.apef.sdef.connector.k8s.K8sConfig;
import io.apef.sdef.connector.tcp.TcpClientConfig;
import io.apef.sdef.connector.tcp.TcpServerConfig;
import io.apef.base.utils.ObjectFormatter;
import io.vertx.core.VertxOptions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class ApefSdefConfig {
    private VertxOptions vertx = new VertxOptions();

    private K8sConfig.K8sFactoryConfig k8sFactory = new K8sConfig.K8sFactoryConfig();
    private ApefClustersConfig clusters = new ApefClustersConfig();
    private VertxHttpClientConfig.VertxHttpClientFactoryConfig httpClientFactory =
            new VertxHttpClientConfig.VertxHttpClientFactoryConfig();
    private VertxHttpServerConfig.VertxServerFactoryConfig httpServerFactory
            = new VertxHttpServerConfig.VertxServerFactoryConfig();

    private TcpClientConfig.TcpClientFactoryConfig tcpClientFactory = new
            TcpClientConfig.TcpClientFactoryConfig();

    private TcpServerConfig.TcpServerFactoryConfig tcpServerFactory = new
            TcpServerConfig.TcpServerFactoryConfig();

    public String toString() {
        return ObjectFormatter.toString(this);
    }

}
