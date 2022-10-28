package eu.xenit.testing.k8s.command;

import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public class CommandHelper {

    public static void executeAndPrintCommand(String... command) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.redirectError();
        builder.command(command);
        try {
            Process process = builder.inheritIO().start();
            assert process.waitFor() == 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static String executeCommandAndRedirectToInputStream(String... command) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.redirectError();
        builder.command(command);
        try {
            Process process = builder.start();
            String result = new String(process.getInputStream().readAllBytes());
            assert process.waitFor() == 0;
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static String[] prependArg(String first, String[] args) {
        var newArgs = new String[args.length+1];
        newArgs[0] = first;
        System.arraycopy(args, 0, newArgs, 1, args.length);
        return newArgs;
    }

}
