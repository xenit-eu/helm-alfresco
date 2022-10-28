package eu.xenit.testing.k8s.cluster.kind;

import eu.xenit.testing.k8s.command.CommandHelper;
import org.jetbrains.annotations.NotNull;

public class KindCommander {

    private String binaryPath;

    public KindCommander(String binaryPath) {
        this.binaryPath = binaryPath;
    }

    public KindCommander() {
        this.binaryPath = "kind";
    }

    public void commandAndPrint(String... args) {
        CommandHelper.executeAndPrintCommand(prependKind(args));
    }

    public String commandReturn(String... args) {
        return CommandHelper.executeCommandAndRedirectToInputStream(prependKind(args));
    }

    @NotNull
    private String[] prependKind(String[] args) {
        return CommandHelper.prependArg(binaryPath, args);
    }

}
