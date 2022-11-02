package eu.xenit.testing.k8s.kind;

import eu.xenit.testing.k8s.cluster.Cluster;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class KindCluster extends Cluster {

    private KindCommander kindCommander;
    private String name;
    public KindCluster(String name, KindCommander kindCommander) {
        super("kind-" + name);
        this.name = name;
        this.kindCommander = kindCommander;
    }

    @Override
    public void destroy() {
        kindCommander.commandAndPrint("delete", "cluster", "--name", name);
    }

    private Path kubeConfig;
    @Override
    public Path getKubeConfig() {
        if (kubeConfig == null) {
            try {
                kubeConfig = Files.createTempFile("kubeconfig-"+name, ".yaml");
                Files.writeString(kubeConfig, kindCommander.commandReturn("get", "kubeconfig", "--name", name));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return kubeConfig;
    }
}
