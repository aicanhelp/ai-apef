package io.apef.base.config.spring.kubernetes;

import io.apef.testing.unit.BaseUnitSpec;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.server.mock.KubernetesMockServer;
import org.testng.annotations.BeforeClass;

public class KubernetesPropertySourceTest extends BaseUnitSpec {
    private static KubernetesMockServer mockServer = new KubernetesMockServer();
    private static KubernetesClient mockClient;

    @BeforeClass
    public void setup() {
        mockServer.init();
        mockClient = mockServer.createClient();

        mockServer.expect().get()
                .withPath("/api/v1/namespaces/testns/configmaps/testapp")
                .andReturn(
                        200,
                        new ConfigMapBuilder()
                                .addToData("spring.kubernetes.test.value", "value1").build())
                .always();
        mockServer.expect().get()
                .withPath("/api/v1/namespaces/testns/secrets/testapp")
                .andReturn(
                        200,
                        new SecretBuilder()
                                .addToData("amq.pwd", "'MWYyZDFlMmU2N2Rm'")
                                .addToData("amq.usr", "'YWRtaW4K'")
                                .build())
                .always();

        //Configure the kubernetes master url to point to the mock server
        System.setProperty(Config.KUBERNETES_MASTER_SYSTEM_PROPERTY, mockClient.getConfiguration().getMasterUrl());
        System.setProperty(Config.KUBERNETES_TRUST_CERT_SYSTEM_PROPERTY, "true");
        System.setProperty(Config.KUBERNETES_AUTH_TRYKUBECONFIG_SYSTEM_PROPERTY, "false");
        System.setProperty(Config.KUBERNETES_AUTH_TRYSERVICEACCOUNT_SYSTEM_PROPERTY, "false");
    }

    public void test() {

    }
}