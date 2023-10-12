package eu.xenit.testing.k8s.kind;

import eu.xenit.testing.k8s.PodTests;
import eu.xenit.testing.k8s.cluster.Cluster;
import eu.xenit.testing.k8s.helm.HelmCommander;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;


class HelmAlfrescoTest {

    @Test
    void smallSetup() throws IOException {
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
                        hostPort: 8099
                        protocol: TCP
                        """;

        var values = """
                general:
                  cni: kindnetd
                ingress:
                  host: test
                  protocol: http
                  kubernetes.io/ingress.class: {}
                acs:
                  replicas: 1
                  resources:
                    requests:
                      memory: "2Gi"
                      cpu: "0"
                mq:
                  resources:
                    requests:
                      memory: "1Gi"
                      cpu: "0"
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

            PodTests.checkPodsReady(cluster, namespace, "app = acs", 1, 300);
            PodTests.checkPodsReady(cluster, namespace, "app = share", 1, 300);
        } finally {
            if (cluster != null) {
                cluster.destroy();
            }
        }

    }
}