package io.apef.sdef.connector.http.server;

import io.apef.base.utils.Constants;
import io.apef.metrics.api.MetricsApi;
import io.apef.metrics.api.MetricsApiAction;
import io.apef.metrics.api.MetricsApiParam;
import io.apef.metrics.config.MetricsApisConfig;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.TimeoutHandler;
import io.vertx.ext.web.handler.CookieHandler;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Accessors(fluent = true)
public class VertxHttpServer {
    public final static int DEFAULT_PORT = 9393;
    protected final static Logger log = LoggerFactory.getLogger(VertxHttpServer.class);
    private Vertx vertx;
    private VertxHttpServerConfig connectorConfig;
    private HttpServer httpServer;
    private Router router;

    public VertxHttpServer(Vertx vertx) {
        this(vertx, defaultServerConfig());
    }

    private static VertxHttpServerConfig defaultServerConfig() {
        VertxHttpServerConfig serverConfig = new VertxHttpServerConfig();
        serverConfig.setName("Default_http_Server")
                .setHost("0.0.0.0")
                .setPort(DEFAULT_PORT);
        return serverConfig;
    }

    public VertxHttpServer(Vertx vertx, VertxHttpServerConfig connectorConfig) {
        this.vertx = vertx;
        this.connectorConfig = connectorConfig;
        this.router = Router.router(vertx);
        this.setCors(connectorConfig.getCors());

        this.router.route()
                .handler(BodyHandler.create());
        this.router.route().handler(CookieHandler.create());
        this.router.route().handler(TimeoutHandler.create(connectorConfig.getTimeout()));
    }

    private void setCors(CorsConfig corsConfig) {
        if (corsConfig == null) return;
        this.router.route().handler(
                CorsHandler.create(corsConfig.getAllowedOriginPattern())
                        .allowedMethods(corsConfig.allowedMethods())
                        .allowedHeaders(corsConfig.allowedHeaders())
        );
    }

    public Vertx vertx() {
        return this.vertx;
    }

    public String routeUri(Route route) {
        return this.routeUri(route.getPath());
    }

    public String routeUri(String route) {
        return Constants.HTTP_SCHEMA + this.connectorConfig.getHost()
                + Constants.S_COLON + this.connectorConfig.getPort()
                + Constants.HTTP_SEPARATOR + route;
    }

    public VertxHttpServer start() {
        if (this.httpServer == null) {
            log.info("Server Connector '{}' Starting on host:{}, port:{} ",
                    this.connectorConfig.getName(), this.connectorConfig.getHost(), this.connectorConfig.getPort());

            this.httpServer = vertx.createHttpServer(this.connectorConfig)
                    .requestHandler(router::accept)
                    .listen(r -> {
                        if (r.succeeded()) {
                            log.info("Server Connector '{}' Started on host:{}, port:{} ",
                                    this.connectorConfig.getName(), this.connectorConfig.getHost(),
                                    this.connectorConfig.getPort());

                        } else {
                            log.error("Failed to start Server Connector on host:" +
                                            this.connectorConfig.getHost() + ",port:" + this.connectorConfig.getPort(),
                                    r.cause()
                            );
                        }
                    });
        }

        return this;
    }

    public void close() {
        if (this.httpServer != null) {
            this.httpServer.close();
        }
    }

    public VertxHttpServer getHandler(String context, Handler<RoutingContext> handler) {
        if (StringUtils.isEmpty(context) || handler == null) return this;

        this.router.get(context).handler(handler);
        log.info("Configure Http GET:{} on connector: {}",
                context, this.connectorConfig.getName());
        return this;
    }

    public VertxHttpServer postHandler(String context, Handler<RoutingContext> handler) {
        if (StringUtils.isEmpty(context) || handler == null) return this;

        this.router.post(context).handler(handler);
        log.info("Configure Http POST:{} on connector: {}",
                context, this.connectorConfig.getName());
        return this;
    }

    //todo
    public VertxHttpServer bindMetrics() {
        MetricsApisConfig apisConfig = MetricsApi.apisContext();
        String path = MetricsApiParam.metricsPathContext(apisConfig.getRootContext());

        this.getHandler(apisConfig.getRootContext() + "*", event -> {
            event.response().end("Please use path: " + path);
        }).getHandler(path, event -> {
            MetricsApiAction action = MetricsApiAction.action(event.request().getParam(MetricsApiParam.action.name()));
            switch (action) {
                case enable:
                    event.response().end("TODO:enable");
                    break;
                case disable:
                    event.response().end("TODO:disable");
                    break;
                case item:
                    event.response().end("TODO:item");
                    break;
                case list:
                    event.response().end("TODO:list");
                    break;
                case query:
                    event.response().end("TODO:query");
                    break;
                case status:
                    event.response().end("TODO:status");
                    break;
                default:
                    event.response().end("TODO:help");
            }
        });
        return this;
    }
}
