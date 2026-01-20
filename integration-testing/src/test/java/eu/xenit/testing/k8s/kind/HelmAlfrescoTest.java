package eu.xenit.testing.k8s.kind;

import com.contentgrid.helm.HelmInstallCommand.InstallOption;
import com.contentgrid.junit.jupiter.helm.HelmChart;
import com.contentgrid.junit.jupiter.helm.HelmChartHandle;
import com.contentgrid.junit.jupiter.helm.HelmClient;
import com.contentgrid.junit.jupiter.k8s.KubernetesTestCluster;
import com.contentgrid.junit.jupiter.k8s.wait.KubernetesResourceWaiter;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KubernetesTestCluster
@HelmClient
class HelmAlfrescoTest {

    Logger logger = LoggerFactory.getLogger(HelmAlfrescoTest.class);

    static KubernetesClient kubernetesClient;

    @HelmChart(chart = "file:../xenit-alfresco")
    static HelmChartHandle alfrescoChart;

    @BeforeAll
    static void beforeAll() {
        var installed = alfrescoChart.install(
                InstallOption.values(Map.of(
                        "general.cni", "default",
                        "ingress.host", "test",
                        "ingress.protocol", "http",
                        "ingress.kubernetes.io/ingress.class", "")),
                InstallOption.values(Map.of(
                        "acs.replicas", 1,
                        "acs.resources.requests.memory", "2Gi",
                        "acs.resources.requests.cpu", "0",
                        "mq.resources.requests.memory", "1Gi",
                        "mq.resources.requests.cpu", "0",
                        "solr.enabled", "false",
                        "transformServices.enabled", "false",
                        "digitalWorkspace.enabled", "false"
                ))
        );

        new KubernetesResourceWaiter(kubernetesClient)
                .include(installed)
                .await(wait -> wait.atMost(5, TimeUnit.MINUTES));
    }

    @Test
    void testPodsReady() throws IOException {
        // if we get here the helm chart has started
        logger.info("PodsReady");
    }

//    @Test
//    void smallSetup() throws IOException {
//        var kindConfiguration = """
//                kind: Cluster
//                apiVersion: kind.x-k8s.io/v1alpha4
//                nodes:
//                  - role: control-plane
//                    kubeadmConfigPatches:
//                      - |
//                        kind: InitConfiguration
//                        nodeRegistration:
//                          kubeletExtraArgs:
//                            node-labels: "ingress-ready=true"
//                    extraPortMappings:
//                      - containerPort: 80
//                        hostPort: 8099
//                        protocol: TCP
//                        """;
//
//        var values = """
//                general:
//                  cni: kindnetd
//                ingress:
//                  host: test
//                  protocol: http
//                  kubernetes.io/ingress.class: {}
//                acs:
//                  replicas: 1
//                  resources:
//                    requests:
//                      memory: "2Gi"
//                      cpu: "0"
//                mq:
//                  resources:
//                    requests:
//                      memory: "1Gi"
//                      cpu: "0"
//                solr:
//                  enabled: false
//                transformServices:
//                  enabled: false
//                digitalWorkspace:
//                  enabled: false
//                """;
//
//        var clusterProvisioner = new KindClusterProvisioner();
//        clusterProvisioner.setConfiguration(kindConfiguration);
//
//        Cluster cluster = null;
//
//        try {
//            cluster = clusterProvisioner.provision();
//
//            var tempFile = Files.createTempFile("values", ".yaml");
//            Files.writeString(tempFile, values);
//
//            var helmCommander = new HelmCommander(cluster);
//            var namespace = "mynamespace";
//            helmCommander.commandAndPrint("install",
//                    "testinstall", "../xenit-alfresco",
//                    "-f", tempFile.toAbsolutePath().toString(),
//                    "-n", namespace, "--create-namespace");
//
//            PodTests.checkPodsReady(cluster, namespace, "app = acs", 1, 300);
//            PodTests.checkPodsReady(cluster, namespace, "app = share", 1, 300);
//        } finally {
//            if (cluster != null) {
//                cluster.destroy();
//            }
//        }
//
//    }
}