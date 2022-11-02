package eu.xenit.testing.k8s.command;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

public class CommandHelper {

        public static void executeAndPrintCommand(Logger logger, String... command) {
            executeAndPrintCommand(logger, false, command);
        }

        public static void executeAndPrintCommand(Logger logger, boolean stdErrAsInfo, String... command) {
        try {
            var executor = new ProcessExecutor().command(command)
                    .redirectOutput(Slf4jStream.of(logger).asInfo());
            if(!stdErrAsInfo) {
                executor = executor.redirectError(Slf4jStream.of(logger).asError());
            }
            assert executor.execute().getExitValue() == 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String executeCommandAndReturnOutput(String... command) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.redirectError();
        builder.command(command);
        try {
            Process process = builder.start();
            String result = new String(process.getInputStream().readAllBytes());
            assert process.waitFor() == 0;
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static String[] prependArg(String first, String[] args) {
        var newArgs = new String[args.length + 1];
        newArgs[0] = first;
        System.arraycopy(args, 0, newArgs, 1, args.length);
        return newArgs;
    }

}
