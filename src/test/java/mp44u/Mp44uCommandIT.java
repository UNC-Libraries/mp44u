package mp44u;

import mp44u.services.AudioService;
import mp44u.services.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @BeforeEach
    public void setup() throws Exception {
        command = new CommandLine(new CLIMain());
        System.setOut(new PrintStream(outputStreamCaptor));

        audioService = new AudioService();
        videoService = new VideoService();
    }

    @Test
    public void testFfmpegAudio() throws Exception {
        String testFile = "src/test/resources/04007_G0010_2_2.mp3";

        String[] args = new String[] {
                "mp44u",
                "audio", "-i", testFile,
                "-o", "src/test/resources/04007_G0010_2_2"
        };

        executeExpectSuccess(args);

        // ffmpeg is unable to create files in tmpdir unless tmpdir is mounted without noexec
        // delete file
        Files.delete(Path.of("src/test/resources/04007_G0010_2_2.m4a"));
        assertFalse(Files.exists(Path.of("src/test/resources/04007_G0010_2_2.m4a")));
    }

    @Test
    public void testFfmpegAudioFail() throws Exception {
        String testFile = "src/test/resources/test.mp3";

        String[] args = new String[] {
                "mp44u",
                "audio", "-i", testFile,
                "-o", "src/test/resources/test"
        };

        executeExpectFailure(args);
    }

    @Test
    public void testFfmpegVideo() throws Exception {
        String testFile = "src/test/resources/AMEN.MOV";

        String[] args = new String[] {
                "mp44u",
                "video", "-i", testFile,
                "-o", "src/test/resources/AMEN"
        };

        executeExpectSuccess(args);

        // delete file
        Files.delete(Path.of("src/test/resources/AMEN.mp4"));
        assertFalse(Files.exists(Path.of("src/test/resources/AMEN.mp4")));
    }

    @Test
    public void testFfmpegVideoFail() throws Exception {
        String testFile = "src/test/resources/test.mp4";

        String[] args = new String[] {
                "mp44u",
                "video", "-i", testFile,
                "-o", "src/test/resources/test"
        };

        executeExpectFailure(args);
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
