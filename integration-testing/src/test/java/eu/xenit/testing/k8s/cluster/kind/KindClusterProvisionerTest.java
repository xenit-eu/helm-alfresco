package eu.xenit.testing.k8s.cluster.kind;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class KindClusterProvisionerTest {

    @Test
    void provision() {
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
        clusterProvisioner.setBinaryPath("/home/thijs/go/bin/kind");
        clusterProvisioner.setConfiguration(configuration);
        clusterProvisioner.provision();
    }
}