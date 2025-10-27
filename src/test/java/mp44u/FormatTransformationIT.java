package mp44u;

import mp44u.options.Mp44uOptions;
import mp44u.services.AudioService;
import mp44u.services.VideoService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Tests for verifying various file format transformations
 * @author krwong
 */
public class FormatTransformationIT {
    private static final Logger log = getLogger(FormatTransformationIT.class);

    private AudioService audioService;
    private VideoService videoService;
    private Path testOutput = Paths.get("target/test_output");

    @BeforeEach
    public void setup() throws Exception {
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
    public void testAudioMp3() throws Exception {
        Path testFile = Path.of("src/test/resources/04007_G0010_2_2.mp3");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(testOutput.resolve("04007_G0010_2_2"));

        Path outputFile = audioService.ffmpegConvertToM4a(options);

        assertTrue(Files.exists(outputFile));
        assertEquals(testOutput.resolve("04007_G0010_2_2.m4a"), outputFile);
    }

    @Test
    public void testAudioAif() throws Exception {
        Path testFile = Path.of("src/test/resources/3AudioTrack.aiff");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(testOutput.resolve("3AudioTrack"));

        Path outputFile = audioService.ffmpegConvertToM4a(options);

        assertTrue(Files.exists(outputFile));
        assertEquals(testOutput.resolve("3AudioTrack.m4a"), outputFile);
    }

    @Test
    public void testAudioAu() throws Exception {
        Path testFile = Path.of("src/test/resources/b00310.au");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(testOutput.resolve("b00310"));

        Path outputFile = audioService.ffmpegConvertToM4a(options);

        assertTrue(Files.exists(outputFile));
        assertEquals(testOutput.resolve("b00310.m4a"), outputFile);
    }

    @Test
    public void testAudioWav() throws Exception {
        Path testFile = Path.of("src/test/resources/14.wav");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(testOutput.resolve("14"));

        Path outputFile = audioService.ffmpegConvertToM4a(options);

        assertTrue(Files.exists(outputFile));
        assertEquals(testOutput.resolve("14.m4a"), outputFile);
    }

    @Test
    public void testAudioWma() throws Exception {
        Path testFile = Path.of("src/test/resources/DS400038.WMA");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(testOutput.resolve("DS400038"));

        Path outputFile = audioService.ffmpegConvertToM4a(options);

        assertTrue(Files.exists(outputFile));
        assertEquals(testOutput.resolve("DS400038.m4a"), outputFile);
    }

    @Test
    public void testVideoMp4() throws Exception {
        Path testFile = Path.of("src/test/resources/009.mp4");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(testOutput.resolve("009_access"));

        Path outputFile = videoService.ffmpegConvertToMp4(options);

        assertTrue(Files.exists(outputFile));
        assertEquals(testOutput.resolve("009_access.mp4"), outputFile);
    }

    @Test
    public void testVideoMov() throws Exception {
        Path testFile = Path.of("src/test/resources/AMEN.MOV");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(testOutput.resolve("AMEN"));

        Path outputFile = videoService.ffmpegConvertToMp4(options);

        assertTrue(Files.exists(outputFile));
        assertEquals(testOutput.resolve("AMEN.mp4"), outputFile);
    }

    @Test
    public void testVideoMts() throws Exception {
        Path testFile = Path.of("src/test/resources/00288.MTS");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(testOutput.resolve("00288"));

        Path outputFile = videoService.ffmpegConvertToMp4(options);

        assertTrue(Files.exists(outputFile));
        assertEquals(testOutput.resolve("00288.mp4"), outputFile);
    }
}
