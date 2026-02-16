package mp44u.util;

import static org.slf4j.LoggerFactory.getLogger;

import mp44u.errors.CommandException;
import mp44u.errors.CommandTimeoutException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * Utility for executing commands
 * @author krwong
 */
public class CommandUtility {
    private static final Logger log = getLogger(CommandUtility.class);

    private CommandUtility() {
    }

    /**
     * Run a given command
     * @param command the command to be executed
     * @return command output
     */
    public static String executeCommand(List<String> command) {
        return executeCommand(command, -1);
    }

    public static String executeCommand(List<String> command, int maxTimeoutSeconds) {
        log.debug("Executing command with timeout {}s: {}", maxTimeoutSeconds, String.join(" ", command));
        CommandLine cmdLine = CommandLine.parse(command.getFirst());
        cmdLine.addArguments(command.subList(1, command.size()).toArray(new String[0]));

        DefaultExecutor executor = DefaultExecutor.builder().get();
        ExecuteWatchdog watchdog = null;
        if (maxTimeoutSeconds > 0) {
            watchdog = ExecuteWatchdog.builder()
                                          .setTimeout(Duration.ofSeconds(maxTimeoutSeconds))
                                          .get();
            executor.setWatchdog(watchdog);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        executor.setStreamHandler(new PumpStreamHandler(outputStream, errorStream));

        try {
            executor.execute(cmdLine);
            return outputStream.toString();
        } catch (ExecuteException e) {
            String output = outputStream.toString();
            int exitValue = e.getExitValue();

            if (watchdog != null && watchdog.killedProcess()) {
                throw new CommandTimeoutException("Command timed out after " + maxTimeoutSeconds + " seconds",
                        command, output);
            }
            throw new CommandException("Command failed to execute", command, output, exitValue, e);
        } catch (IOException e) {
            String output = outputStream + "\n" + errorStream;
            throw new CommandException("Command failed to execute", command, output, e);
        }
    }
}
