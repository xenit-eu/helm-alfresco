package eu.xenit.testing.k8s.alfresco;

import eu.xenit.testing.k8s.cluster.Cluster;
import eu.xenit.testing.k8s.cluster.ClusterProvisioner;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Random;
import java.util.Scanner;

public class KindClusterProvisioner implements ClusterProvisioner {
    private KindCommander kindCommander;
    public KindClusterProvisioner(KindCommander kindCommander) {
        this.kindCommander = kindCommander;
    }

    public KindClusterProvisioner() {
        this.kindCommander = new KindCommander();
    }

    private String configuration;

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    private Random random = new Random();
    @Override
    public Cluster provision() {
        String name = random.ints(97, 123)
                .limit(10)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        kindCommander.commandAndPrint("create", "cluster", "--name", name);
        return new KindCluster(name, kindCommander);
    }

    private static void inheritIO(final InputStream src, final PrintStream dest) {
        new Thread(new Runnable() {
            public void run() {
                Scanner sc = new Scanner(src);
                while (sc.hasNextLine()) {
                    dest.println(sc.nextLine());
                }
            }
        }).start();
    }
}
