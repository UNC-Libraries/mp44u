package mp44u.services;

import mp44u.options.Mp44uOptions;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.MockitoAnnotations.openMocks;

public class AudioServiceTest {
    private AutoCloseable closeable;
    private AudioService service;

    @BeforeEach
    public void setup() throws Exception {
        closeable = openMocks(this);
        service = new AudioService();

        if (Files.notExists(Paths.get("target/test_output"))) {
            Files.createDirectory(Paths.get("target/test_output"));
        }
    }

    @AfterEach
    public void close() throws Exception {
        FileUtils.deleteDirectory(new File("target/test_output"));
        closeable.close();
    }

    @Test
    public void testAudioMp3() throws Exception {
        Path testFile = Path.of("src/test/resources/04007_G0010_2_2.mp3");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(Paths.get("target/test_output/04007_G0010_2_2"));

        Path outputFile = service.ffmpegConvertToM4a(options);

        assertTrue(Files.exists(outputFile));
        assertEquals(Path.of("target/test_output/04007_G0010_2_2.m4a"), outputFile);
    }

    @Test
    public void testAudioAif() throws Exception {
        Path testFile = Path.of("src/test/resources/3AudioTrack.aiff");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(Path.of("target/test_output/3AudioTrack"));

        Path outputFile = service.ffmpegConvertToM4a(options);

        assertTrue(Files.exists(outputFile));
        assertEquals(Path.of("target/test_output/3AudioTrack.m4a"), outputFile);
    }

    @Test
    public void testAudioAu() throws Exception {
        Path testFile = Path.of("src/test/resources/b00310.au");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(Path.of("target/test_output/b00310"));

        Path outputFile = service.ffmpegConvertToM4a(options);

        assertTrue(Files.exists(outputFile));
        assertEquals(Path.of("target/test_output/b00310.m4a"), outputFile);
    }

    @Test
    public void testAudioWav() throws Exception {
        Path testFile = Path.of("src/test/resources/14.wav");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(Path.of("target/test_output/14"));

        Path outputFile = service.ffmpegConvertToM4a(options);

        assertTrue(Files.exists(outputFile));
        assertEquals(Path.of("target/test_output/14.m4a"), outputFile);
    }

    @Test
    public void testAudioWma() throws Exception {
        Path testFile = Path.of("src/test/resources/DS400038.WMA");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(Path.of("target/test_output/DS400038"));

        Path outputFile = service.ffmpegConvertToM4a(options);

        assertTrue(Files.exists(outputFile));
        assertEquals(Path.of("target/test_output/DS400038.m4a"), outputFile);
    }
}
