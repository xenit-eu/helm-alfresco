package eu.xenit.testing.k8s.helm;

import eu.xenit.testing.k8s.cluster.Cluster;
import eu.xenit.testing.k8s.command.CommandHelper;
import java.util.ArrayList;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelmCommander {

    private static Logger logger = LoggerFactory.getLogger(HelmCommander.class);

    private final Cluster cluster;

    public HelmCommander(Cluster cluster) {
        this.cluster = cluster;
    }

    public void commandAndPrint(String... args) {
        var argList = new ArrayList(Arrays.stream(CommandHelper.prependArg("helm", args)).toList());
        argList.add("--kubeconfig");
        argList.add(cluster.getKubeConfig().toAbsolutePath().toString());
        String[] argArray = (String[]) argList.toArray(new String[argList.size()]);
        CommandHelper.executeAndPrintCommand(logger, argArray);
    }


}
