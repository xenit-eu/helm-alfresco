package eu.xenit.testing.k8s.cluster.kind;

import static org.awaitility.Awaitility.await;

import eu.xenit.testing.k8s.cluster.Cluster;
import eu.xenit.testing.k8s.helm.HelmCommander;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;



class KindClusterProvisionerTest {

    @Test
    void provision() throws IOException {
        var kindConfiguration = """
                kind: Cluster
                apiVersion: kind.x-k8s.io/v1alpha4
                nodes:
                  - role: control-plane
                    kubeadmConfigPatches:
                      - |
                        kind: InitConfiguration
                        nodeRegistration:
                          kubeletExtraArgs:
                            node-labels: "ingress-ready=true"
                    extraPortMappings:
                      - containerPort: 80
                        hostPort: 80
                        protocol: TCP
                        """;

        var values = """
                general:
                  cni: kindnetd
                ingress:
                  host: test
                  protocol: http
                  kubernetes.io/ingress.class: {}
                imageCredentials:
                  private:
                    username: hello
                    password: world
                  alfresco:
                    username: hello
                    password: world
                acs:
                  resources:
                    requests:
                      memory: "1Gi"
                      cpu: "1"
                mq:
                  resources:
                    requests:
                      memory: "1Gi"
                      cpu: "1"
                solr:
                  enabled: false
                transformServices:
                  enabled: false
                digitalWorkspace:
                  enabled: false
                """;

        var clusterProvisioner = new KindClusterProvisioner();
        clusterProvisioner.setConfiguration(kindConfiguration);
        
        Cluster cluster = null;
        
        try {
            cluster = clusterProvisioner.provision();

            var tempFile = Files.createTempFile("values", ".yaml");
            Files.writeString(tempFile, values);

            var helmCommander = new HelmCommander(cluster);
            var namespace = "mynamespace";
            helmCommander.commandAndPrint("install",
                    "testinstall", "../xenit-alfresco",
                    "-f", tempFile.toAbsolutePath().toString(),
                    "-n", namespace, "--create-namespace");

            Cluster finalCluster = cluster;
            await().atMost(10, TimeUnit.MINUTES).until(() -> checkPodsReady(finalCluster, namespace, "app = acs", 1));

        } finally {
            cluster.destroy();
        }
        
    }

    @NotNull
    private static Boolean checkPodsReady(Cluster cluster, String namespace, String labelSelector, int amount) throws ApiException {
        ApiClient apiClient = null;
        try {
            apiClient = Config.fromConfig(cluster.getKubeConfig().toAbsolutePath().toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        CoreV1Api api = new CoreV1Api(apiClient);
        var podList = api.listNamespacedPod(namespace, null, null, null, null, labelSelector, null, null, null, null, null);
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
}