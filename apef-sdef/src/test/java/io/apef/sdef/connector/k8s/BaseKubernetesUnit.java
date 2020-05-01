package io.apef.sdef.connector.k8s;

import io.fabric8.kubernetes.server.mock.KubernetesServer;

public interface BaseKubernetesUnit {
    KubernetesServer server = newKubernetesServer();

    static KubernetesServer newKubernetesServer() {
        KubernetesServer server = new KubernetesServer();
        server.before();
        return server;
    }
}
