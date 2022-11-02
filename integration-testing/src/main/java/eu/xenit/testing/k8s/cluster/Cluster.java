package eu.xenit.testing.k8s.cluster;

import java.io.InputStream;
import java.nio.file.Path;

public abstract class Cluster {

    public Cluster(String context) {
        this.context = context;
    }

    private String context;

    public String getContext() {
        return context;
    }

    public abstract void destroy();

    public abstract Path getKubeConfig();

}
