package eu.xenit.testing.k8s.cluster.kind;

import eu.xenit.testing.k8s.cluster.Cluster;
import eu.xenit.testing.k8s.cluster.ClusterProvisioner;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class KindClusterProvisioner implements ClusterProvisioner {

    public String getBinaryPath() {
        return binaryPath;
    }

    public void setBinaryPath(String binaryPath) {
        this.binaryPath = binaryPath;
    }

    private String binaryPath;

    private String configuration;

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    @Override
    public Cluster provision() {
        try {
            Path temp = Files.createTempFile("kind", ".yml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (configuration != null) {

        }
        ProcessBuilder builder = new ProcessBuilder();
        builder.redirectError();

        builder.command(getBinaryPath(), "create", "cluster", "--name", "javakind");
        try {
            Process process = builder.inheritIO().start();
            assert process.waitFor() == 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
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
