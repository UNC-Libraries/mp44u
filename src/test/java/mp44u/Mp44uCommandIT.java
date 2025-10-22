package mp44u;

import mp44u.services.AudioService;
import mp44u.services.VideoService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.slf4j.LoggerFactory.getLogger;

public class Mp44uCommandIT {
    private static final Logger log = getLogger(Mp44uCommandIT.class);
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    protected final PrintStream originalOut = System.out;
    protected final ByteArrayOutputStream out = new ByteArrayOutputStream();
    protected String output;

    protected CommandLine command;

    private AudioService audioService;
    private VideoService videoService;
    private Path testOutput = Paths.get("target/test_output");

    @BeforeEach
    public void setup() throws Exception {
        command = new CommandLine(new CLIMain());
        System.setOut(new PrintStream(outputStreamCaptor));

        audioService = new AudioService();
        videoService = new VideoService();

        if (Files.notExists(testOutput)) {
            Files.createDirectory(testOutput);
        }
    }

    @AfterEach
    public void close() throws Exception {
        FileUtils.deleteDirectory(testOutput.toFile());
    }

    @Test
    public void testFfmpegAudio() throws Exception {
        String testFile = "src/test/resources/04007_G0010_2_2.mp3";

        String[] args = new String[] {
                "mp44u",
                "audio", "-i", testFile,
                "-o", testOutput.resolve("04007_G0010_2_2").toString()
        };

        executeExpectSuccess(args);
    }

    @Test
    public void testFfmpegAudioInputFileDoesNotExist() throws Exception {
        String testFile = "src/test/resources/test.mp3";

        String[] args = new String[] {
                "mp44u",
                "audio", "-i", testFile,
                "-o", testOutput.resolve("test").toString()
        };

        executeExpectFailure(args);
        assertTrue(Files.notExists(Path.of("target/test_output/test.m4a")));
    }

    @Test
    public void testFfmpegVideo() throws Exception {
        String testFile = "src/test/resources/AMEN.MOV";

        String[] args = new String[] {
                "mp44u",
                "video", "-i", testFile,
                "-o", testOutput.resolve("AMEN").toString()
        };

        executeExpectSuccess(args);
    }

    @Test
    public void testFfmpegVideoInputFileDoesNotExist() throws Exception {
        String testFile = "src/test/resources/test.mp4";

        String[] args = new String[] {
                "mp44u",
                "video", "-i", testFile,
                "-o", testOutput.resolve("test_access").toString()
        };

        executeExpectFailure(args);
        assertTrue(Files.notExists(Path.of("target/test_output/test_access.mp4")));
    }

    protected void executeExpectSuccess(String[] args) {
        int result = command.execute(args);
        output = out.toString();
        if (result != 0) {
            System.setOut(originalOut);
            // Can't see the output from the command without this
            System.out.println(output);
            fail("Expected command to result in success: " + String.join(" ", args) + "\nWith output:\n" + output);
        }
    }

    protected void executeExpectFailure(String[] args) {
        int result = command.execute(args);
        output = out.toString();
        if (result == 0) {
            System.setOut(originalOut);
            log.error(output);
            fail("Expected command to result in failure: " + String.join(" ", args));
        }
    }
}
