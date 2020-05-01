package io.apef.sdef.discovery;

import io.apef.testing.unit.BaseUnitSpec;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.*;
import io.fabric8.kubernetes.server.mock.*;
import org.testng.annotations.Test;

import java.net.HttpURLConnection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

@Test(enabled = false)
public class KubernetesTest extends BaseUnitSpec {
    private KubernetesServer server = new KubernetesServer(false);

    Pod pod1 = new PodBuilder().withNewMetadata().withNamespace("test").withName("pod1")
            .withResourceVersion("1").endMetadata().build();

    Service service1 = new ServiceBuilder().withNewMetadata()
            .withNamespace("test").withName("service1")
            .withResourceVersion("1").endMetadata().build();

    Namespace namespace1 = new NamespaceBuilder().withNewMetadata()
            .withName("test").withResourceVersion("1").endMetadata().build();

    static final Status outdatedStatus = new StatusBuilder().withCode(HttpURLConnection.HTTP_GONE)
            .withMessage(
                    "401: The event in requested index is outdated and cleared (the requested history has been cleared [3/1]) [2]")
            .build();
    static final WatchEvent outdatedEvent = new WatchEventBuilder().withStatusObject(outdatedStatus).build();

    @Override
    protected void doBeforeClass() {
        server.before();
    }

    @Override
    protected void doAfterClass() {
        log.info("Closing Kubernetes Server...");
        server.after();
    }


    public void testServiceDeletedAndOutdated() throws InterruptedException {
        KubernetesClient client = server.getClient().inNamespace("test");

        // DELETED event, then history outdated
        server.expect()
                .withPath("/api/v1/namespaces/test/services?fieldSelector=metadata.name%3Dservice1&resourceVersion=1&watch=true")
                .andUpgradeToWebSocket().open().waitFor(200).andEmit(new WatchEvent(service1, "DELETED")).waitFor(200)
//                .andEmit(new WatchEvent(pod1, "MODIFIED")).done().once();
                .andEmit(outdatedEvent).done().once();

        final Blocker deleteLatch = new Blocker();
        final Blocker closeLatch = new Blocker();

        Watch watch = client.services().withName("service1").withResourceVersion("1").watch(new Watcher<Service>() {
            @Override
            public void eventReceived(Action action, Service resource) {
                switch (action) {
                    case DELETED:
                        deleteLatch.end();
                        break;
                    default:
                        deleteLatch.failAndEnd("Invalid action: " + action);
                }
            }

            @Override
            public void onClose(KubernetesClientException cause) {
                closeLatch.end();
            }
        });
        //Watch Auto close
        {
            deleteLatch.awaitEnd();
            watch.close();
            closeLatch.awaitEnd();
        }
    }

    public void testWatchPods() throws InterruptedException {
        KubernetesClient client = server.getClient().inNamespace("test");

        // DELETED event, then history outdated
        server.expect()
                .withPath("/api/v1/namespaces?fieldSelector=metadata.name%3Dtest&resourceVersion=1&watch=true")
                .andUpgradeToWebSocket().open().waitFor(200).andEmit(new WatchEvent(namespace1, "DELETED")).waitFor(200)
//                .andEmit(new WatchEvent(pod1, "MODIFIED")).done().once();
                .andEmit(outdatedEvent).done().once();

        final Blocker deleteLatch = new Blocker();
        final Blocker closeLatch = new Blocker();


        Watch watch = client.namespaces().withName("test").withResourceVersion("1").watch(new Watcher<Namespace>() {
            @Override
            public void eventReceived(Action action, Namespace resource) {
                switch (action) {
                    case MODIFIED:

                        log.info("-----" + action);
                        break;
                    case DELETED:
                        deleteLatch.end();
                        break;
                    default:
                        deleteLatch.failAndEnd("Invalid action: " + action);
                }
            }

            @Override
            public void onClose(KubernetesClientException cause) {
                closeLatch.end();
            }
        });
        //Watch Auto close
        {
            deleteLatch.awaitEnd();
            watch.close();
            closeLatch.awaitEnd();
        }
    }

    public void testDeletedAndOutdated() throws InterruptedException {
        KubernetesClient client = server.getClient().inNamespace("test");

        // DELETED event, then history outdated
        server.expect()
                .withPath("/api/v1/namespaces/test/pods?fieldSelector=metadata.name%3Dpod1&resourceVersion=1&watch=true")
                .andUpgradeToWebSocket().open().waitFor(200).andEmit(new WatchEvent(pod1, "DELETED")).waitFor(200)
//                .andEmit(new WatchEvent(pod1, "MODIFIED")).done().once();
                .andEmit(outdatedEvent).done().once();

        final Blocker deleteLatch = new Blocker();
        final Blocker closeLatch = new Blocker();

        Watch watch = client.pods().withName("pod1").withResourceVersion("1").watch(new Watcher<Pod>() {
            @Override
            public void eventReceived(Action action, Pod resource) {
                switch (action) {
                    case DELETED:
                        deleteLatch.end();
                        break;
                    default:
                        deleteLatch.failAndEnd("Invalid action: " + action);
                }
            }

            @Override
            public void onClose(KubernetesClientException cause) {
                closeLatch.end();
            }
        });
        //Watch Auto close
        {
            deleteLatch.awaitEnd();
            watch.close();
            closeLatch.awaitEnd();
        }
    }

    @Test(enabled = false, expectedExceptions = {KubernetesClientException.class})
    public void testHttpErrorWithOutdated() {
        KubernetesClient client = server.getClient().inNamespace("test");
        // http error: history outdated
        server.expect()
                .withPath("/api/v1/namespaces/test/pods?fieldSelector=metadata.name%3Dpod1&resourceVersion=1&watch=true")
                .andReturn(410, outdatedEvent).once();

        try (Watch watch = client.pods().withName("pod1").withResourceVersion("1").watch(new Watcher<Pod>() {
            @Override
            public void eventReceived(Action action, Pod resource) {
            }

            @Override
            public void onClose(KubernetesClientException cause) {
            }
        })) {

        }
    }

    public void testHttpErrorReconnect() throws InterruptedException {
        String path = "/api/v1/namespaces/test/pods?fieldSelector=metadata.name%3Dpod1&resourceVersion=1&watch=true";
        KubernetesClient client = server.getClient()
                .inNamespace("test");
        client.getConfiguration().setConnectionTimeout(200);

        client.getConfiguration().setWatchReconnectInterval(10);

        // accept watch and disconnect
        server.expect().withPath(path).andUpgradeToWebSocket().open().done().once();
        // refuse reconnect attempts 6 times
        server.expect().withPath(path).andReturn(503, new StatusBuilder().withCode(503).build()).times(6);
        // accept next reconnect and send outdated event to stop the watch
        server.expect().withPath(path).andUpgradeToWebSocket().open(outdatedEvent).done().once();

        Blocker blocker = new Blocker();
        try (Watch watch = client.pods().withName("pod1").withResourceVersion("1").watch(new Watcher<Pod>() {
            @Override
            public void eventReceived(Action action, Pod resource) {
                throw new RuntimeException();
            }

            @Override
            public void onClose(KubernetesClientException cause) {
                log.debug("onClose", cause);
                blocker.end();
            }
        })) /* autoclose */ {
            blocker.awaitEnd();
        }
    }

    public void testOnCloseEvent() throws InterruptedException {
        final CountDownLatch eventLatch = new CountDownLatch(2);
        final CountDownLatch closeLatch = new CountDownLatch(1);
        KubernetesClient client = server.getClient().inNamespace("test");

        server.expect()
                .withPath("/api/v1/namespaces/test/pods?fieldSelector=metadata.name%3Dpod1&resourceVersion=1&watch=true")
                .andUpgradeToWebSocket().open().waitFor(200).andEmit(new WatchEvent(pod1, "MODIFIED")).waitFor(200)
                .andEmit(new WatchEvent(pod1, "MODIFIED"))
                .waitFor(100).andEmit(outdatedEvent)
                .done().once();

        Watch watch = client.pods().withName("pod1").withResourceVersion("1").watch(new Watcher<Pod>() {
            @Override
            public void eventReceived(Action action, Pod resource) {
                eventLatch.countDown();
                log.info("--------" + action);
            }

            @Override
            public void onClose(KubernetesClientException cause) {
                closeLatch.countDown();
                log.info("2-------");
            }
        });
        {

            assertTrue(eventLatch.await(10, TimeUnit.SECONDS));
            watch.close();
            assertTrue(closeLatch.await(10, TimeUnit.SECONDS));
        }
    }
}
