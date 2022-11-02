package eu.xenit.testing.k8s.kind;

import eu.xenit.testing.k8s.command.CommandHelper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KindCommander {

    private static Logger logger = LoggerFactory.getLogger(KindCommander.class);

    private String binaryPath;

    public KindCommander(String binaryPath) {
        this.binaryPath = binaryPath;
    }

    public KindCommander() {
        this("kind");
    }

    public void commandAndPrint(String... args) {
        CommandHelper.executeAndPrintCommand(logger, true, prependKind(args));
    }

    public String commandReturn(String... args) {
        return CommandHelper.executeCommandAndReturnOutput(prependKind(args));
    }

    @NotNull
    private String[] prependKind(String[] args) {
        return CommandHelper.prependArg(binaryPath, args);
    }

}
