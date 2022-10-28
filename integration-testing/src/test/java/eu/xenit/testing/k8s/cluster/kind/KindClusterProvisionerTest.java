package eu.xenit.testing.k8s.cluster.kind;

import static org.awaitility.Awaitility.await;

import eu.xenit.testing.k8s.cluster.Cluster;
import eu.xenit.testing.k8s.helm.HelmCommander;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;



class KindClusterProvisionerTest {

    @Test
    void provision() throws IOException, ApiException {
        var configuration = """
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

        var clusterProvisioner = new KindClusterProvisioner();
        clusterProvisioner.setConfiguration(configuration);
        
        Cluster cluster = null;
        
        try {
            cluster = clusterProvisioner.provision();

            var values = """
                general:
                  networkPolicies:
                    enabled: false
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

            var tempFile = Files.createTempFile("values", ".yaml");
            Files.writeString(tempFile, values);

            var helmCommander = new HelmCommander(cluster);
            helmCommander.commandAndPrint("install",
                    "testinstall", "/home/thijs/IdeaProjects/helm-alfresco/xenit-alfresco",
                    "-f", tempFile.toAbsolutePath().toString(),
                    "-n", "mynamespace", "--create-namespace");

            var apiClient = Config.fromConfig(cluster.getKubeConfig().toAbsolutePath().toString());
            CoreV1Api api = new CoreV1Api(apiClient);

            await().atMost(5, TimeUnit.MINUTES).until(() -> {
                var podList = api.listNamespacedPod("mynamespace", null, null, null, null, "app = acs", null, null, null, null, null);
                if (podList.getItems().size() != 1) {
                    return false;
                }
                for (var pod: podList.getItems()) {
                    boolean ready = false;
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
            });

        } finally {
            cluster.destroy();
        }
        
    }
}