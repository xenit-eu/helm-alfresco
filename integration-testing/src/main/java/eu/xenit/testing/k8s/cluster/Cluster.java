package eu.xenit.testing.k8s.cluster;

import java.nio.file.Path;

public interface Cluster {

    String getContext();

    void destroy();

}
