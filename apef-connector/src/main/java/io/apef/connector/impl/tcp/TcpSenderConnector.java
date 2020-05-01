package io.apef.connector.impl.tcp;

import io.apef.core.utils.ChannelTimer;
import io.apef.sdef.ApefSdef;
import io.apef.sdef.connector.tcp.ITcpClient;
import io.apef.connector.base.TxInf;
import io.apef.connector.sender.SenderConnector;
import io.apef.connector.sender.SenderRequestContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TcpSenderConnector extends SenderConnector<TcpRequestContext> {
    private final static Exception TIMEOUT_EX = new Exception("Request Timeout on Tcp Sender:");
    private TcpSenderChannelConfig config;
    private ITcpClient tcpClient;
    private Map<Integer, TcpRequestContext> requestContextMap =
            new ConcurrentHashMap<>();

    public TcpSenderConnector(TcpSenderChannelConfig config) {
        this.config = config;
        this.initClient(config.getTcpClientName());
    }

    public TcpSenderConnector() {
        this.initClient(null);
    }

    private void initClient(String name) {
        this.tcpClient = ApefSdef.tcpClient(name);
        this.tcpClient.onResponse(response -> {
            TcpRequestContext context;
            TxInf txInf = null;
            if (response.isFailOnClient()) {
                context = requestContextMap.remove(response.txId());
            } else {
                txInf = TxInf.from(response.responseData());
                context = requestContextMap.remove(txInf.transactionId());
            }
            if (context == null) {
                return;
            }

            context.timeout().cancel();
            context.HandleResponse(txInf, response);
        });
    }


    @Override
    protected TcpRequestContext newConnectorContext(SenderRequestContext<TcpRequestContext, ?, ?, ?> requestContext) {
        TcpRequestContext context = new TcpRequestContext(tcpClient, requestContext);
        context.timeout(ChannelTimer.timer()
                .newTimeout(timeout -> {
                    requestContextMap.remove(requestContext.txInf().transactionId());
                    requestContext.fail(TIMEOUT_EX.getMessage() + requestContext.txInf(), TIMEOUT_EX);
                }, config.getTimeout(), config.getTimeUnit()));
        this.requestContextMap.put(requestContext.txInf().transactionId(), context);
        return context;
    }
}
