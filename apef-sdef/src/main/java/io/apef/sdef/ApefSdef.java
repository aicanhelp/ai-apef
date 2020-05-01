package io.apef.sdef;

import io.apef.sdef.cluster.ApefCluster;
import io.apef.sdef.cluster.ApefClusterFactory;
import io.apef.sdef.connector.http.VertxHttpClientFactory;
import io.apef.sdef.connector.http.VertxHttpServerFactory;
import io.apef.sdef.connector.tcp.ITcpClient;
import io.apef.sdef.connector.tcp.ITcpServer;
import io.apef.sdef.connector.tcp.TcpClientFactory;
import io.apef.sdef.connector.tcp.TcpServerFactory;
import io.apef.base.config.factory.XFactoryBase;
import io.apef.base.config.utils.ConfigurationLoader;
import io.apef.sdef.connector.http.server.VertxHttpServer;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApefSdef {
    private final static String DEFAULT = "default";
    private final static String configFile = "apef-connector-config.xml";
    private static ApefSdefConfig config = vertxConnectorFactoryConfig();
    private final static Vertx vertx = vertx();

    static ApefSdefConfig vertxConnectorFactoryConfig() {
        ApefSdefConfig config = ConfigurationLoader
                .loadConfiguration(ApefSdefConfig.class, configFile);
        if (config == null) {
            return new ApefSdefConfig();
        }

        return config;
    }

    public static Vertx vertx() {
        if (vertx != null) return vertx;
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        return Vertx.vertx(vertxConnectorFactoryConfig().getVertx());
    }

    @AllArgsConstructor
    private enum InstanceHolder {
        //        K8S("k8s", new K8sFactory(config.getK8sFactory())),
        CLUSTER("cluster", new ApefClusterFactory(config.getClusters().factoryConfig())),
        HTTP_CLIENT("httpClient", new VertxHttpClientFactory(vertx, config.getHttpClientFactory())),
        HTTP_SERVER("httpServer", new VertxHttpServerFactory(vertx, config.getHttpServerFactory())),
        TCP_CLIENT("tcpClient", new TcpClientFactory(config.getTcpClientFactory())),
        TCP_SERVER("tcpServer", new TcpServerFactory(config.getTcpServerFactory()));

        private String type;
        private XFactoryBase<?, ?> factory;

        public <T> T get(String name) {
            try {
                return (T) factory.instance(name);
            } catch (Exception ex) {
                if (log.isDebugEnabled()) {
                    log.warn("Failed on get " + type + " configuration for name: " + name, ex);
                } else
                    log.warn("Failed on get " + type + " configuration for name: " + name);
            }
            return null;
        }

    }

    private static class DefaultServerHolder {
        private static VertxHttpServer defaultHttpServer = createDefaultHttpServer();

        static VertxHttpServer createDefaultHttpServer() {
            VertxHttpServer httpServer = InstanceHolder.HTTP_SERVER.get("default");
            if (httpServer == null) {
                httpServer = new VertxHttpServer(vertx());
                httpServer.start();
            }
            return httpServer;
        }
    }

    private static class DefaultClientHolder {
        private static HttpClient defaultHttpClient = createDefaultHttpClient();

        static HttpClient createDefaultHttpClient() {
            HttpClient httpClient = InstanceHolder.HTTP_CLIENT.get("default");
            if (httpClient == null) {
                return vertx().createHttpClient(
                        new HttpClientOptions()
                                .setMaxPoolSize(16)
                                .setConnectTimeout(3000)
                                .setKeepAlive(true)
                                .setIdleTimeout(30000)
                                .setTcpNoDelay(true)
                                .setPipelining(false)
                                .setReceiveBufferSize(8192)
                                .setReuseAddress(true)
                );
            }

            return httpClient;
        }
    }

    public static HttpClient defaultHttpClient() {
        return DefaultClientHolder.defaultHttpClient;
    }

    public static VertxHttpServer defaultHttpServer() {
        return DefaultServerHolder.defaultHttpServer;
    }

    public static HttpClient httpClient(String name) {
        if (name == null || DEFAULT.equalsIgnoreCase(name)) {
            return DefaultClientHolder.defaultHttpClient;
        }
        HttpClient httpClient = InstanceHolder.HTTP_CLIENT.get(name);
        if (httpClient != null) return httpClient;
        return DefaultClientHolder.defaultHttpClient;
    }

    public static VertxHttpServer httpServer(String name) {
        if (name == null || DEFAULT.equalsIgnoreCase(name)) {
            return DefaultServerHolder.defaultHttpServer;
        }
        VertxHttpServer httpServer = InstanceHolder.HTTP_SERVER.get(name);
        if (httpServer != null) return httpServer;
        log.warn("Can not find the specified http server with name: " + name + ", the default http server will be used.");
        return DefaultServerHolder.defaultHttpServer;
    }

    public static ApefCluster cluster(String name) {
        return InstanceHolder.CLUSTER.get(name);
    }

    public static ITcpClient tcpClient(String name) {
        return InstanceHolder.TCP_CLIENT.get(name);
    }

    public static ITcpServer tcpServer(String name) {
        return InstanceHolder.TCP_SERVER.get(name);
    }
}
