package mp44u.util;

import static org.slf4j.LoggerFactory.getLogger;

import mp44u.errors.CommandException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Utility for executing commands
 * @author krwong
 */
public class CommandUtility {
    private static final Logger log = getLogger(CommandUtility.class);
    private static final int MAX_TIMEOUT_SECONDS = System.getProperty("mp44u.subcommand.timeout") != null ?
            Integer.parseInt(System.getProperty("mp44u.subcommand.timeout")) : 60 * 5;

    private CommandUtility() {
    }

    /**
     * Run a given command
     * @param command the command to be executed
     * @return command output
     */
    public static String executeCommand(List<String> command) {
        log.debug("Executing command with timeout {}s: {}", MAX_TIMEOUT_SECONDS, String.join(" ", command));
        CommandLine cmdLine = CommandLine.parse(command.getFirst());
        cmdLine.addArguments(command.subList(1, command.size()).toArray(new String[0]));

        DefaultExecutor executor = DefaultExecutor.builder().get();
        var watchdog = ExecuteWatchdog.builder()
                                      .setTimeout(Duration.of(MAX_TIMEOUT_SECONDS, ChronoUnit.SECONDS))
                                      .get();
        executor.setWatchdog(watchdog);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        executor.setStreamHandler(new PumpStreamHandler(outputStream, errorStream));

        try {
            int exitValue = executor.execute(cmdLine);
            if (watchdog.killedProcess()) {
                log.warn("Command timed out after {}s: {}", MAX_TIMEOUT_SECONDS, String.join(" ", command));
                throw new CommandException("Command timed out", command, outputStream.toString(), exitValue);
            }
            return outputStream.toString();
        } catch (IOException e) {
            String output = outputStream + "\n" + errorStream;
            throw new CommandException("Command failed to execute", command, output, e);
        }
    }
}
