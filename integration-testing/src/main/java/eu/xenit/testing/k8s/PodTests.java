package eu.xenit.testing.k8s;

import static org.awaitility.Awaitility.await;

import eu.xenit.testing.k8s.cluster.Cluster;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.Config;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.awaitility.core.ConditionTimeoutException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PodTests {

    private static Logger logger = LoggerFactory.getLogger(PodTests.class);

    public static void checkPodsReady(Cluster cluster, String namespace, String labelSelector, int podCount, int timeoutSeconds) {
        try {
            await().atMost(timeoutSeconds, TimeUnit.SECONDS).until(() -> checkPodsReadyInternal(cluster, namespace, labelSelector, podCount));
        } catch (ConditionTimeoutException e) {
            var podList = getV1PodList(cluster, namespace, labelSelector);
            if (podList.getItems().isEmpty()) {
                logger.error("Podlist is empty");
            } else {
                logger.error("Number of pods for %s: %d".formatted(labelSelector, podList.getItems().size()));
                var api = getCoreV1Api(cluster);
                for (var pod: podList.getItems()) {
                    try {
                        var events = api.listNamespacedEvent(namespace, null, null, null, "involvedObject.name="+pod.getMetadata().getName(), null, null, null, null, null, null);
                        logger.error("Events for pod: "+pod.getMetadata().getName());
                        for (var event: events.getItems()) {
                            logger.error("Type: %s, Reason:%s, First time:%s, Last time:%s, Count:%s, From:%s, Message:%s".formatted(event.getType(), event.getReason(), event.getFirstTimestamp(), event.getLastTimestamp(), event.getCount(), event.getSource(), event.getMessage()));
                        }

                    } catch (ApiException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            throw e;
        }
    }

    @NotNull
    private static Boolean checkPodsReadyInternal(
            Cluster cluster, String namespace, String labelSelector, int amount) throws ApiException {
        V1PodList podList = getV1PodList(cluster, namespace, labelSelector);
        if (podList.getItems().size() != amount) {
            return false;
        }
        for (var pod: podList.getItems()) {
            boolean ready = false;
            if (pod.getStatus().getConditions() == null) {
                return false;
            }
            for (var condition: pod.getStatus().getConditions()) {
                if ("Ready".equals(condition.getType()) && "True".equals(condition.getStatus())) {
                    ready = true;
                    break;
                }
            }
            if (!ready) {
                return false;
            }
        }
        return true;
    }

    private static V1PodList getV1PodList(Cluster cluster, String namespace, String labelSelector) {
        CoreV1Api api = getCoreV1Api(cluster);
        V1PodList podList = null;
        try {
            podList = api.listNamespacedPod(namespace, null, null, null, null, labelSelector, null, null, null, null, null);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        return podList;
    }

    @NotNull
    private static CoreV1Api getCoreV1Api(Cluster cluster) {
        ApiClient apiClient = null;
        try {
            apiClient = Config.fromConfig(cluster.getKubeConfig().toAbsolutePath().toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        CoreV1Api api = new CoreV1Api(apiClient);
        return api;
    }

}
