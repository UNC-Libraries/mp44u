package mp44u.services;

import mp44u.options.Mp44uOptions;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.MockitoAnnotations.openMocks;

public class AudioServiceTest {
    private AutoCloseable closeable;
    private AudioService service;
    private Path testOutput = Paths.get("target/test_output");

    @BeforeEach
    public void setup() throws Exception {
        closeable = openMocks(this);
        service = new AudioService();

        if (Files.notExists(testOutput)) {
            Files.createDirectory(testOutput);
        }
    }

    @AfterEach
    public void close() throws Exception {
        FileUtils.deleteDirectory(testOutput.toFile());
        closeable.close();
    }

    @Test
    public void testAudioMp3() throws Exception {
        Path testFile = Path.of("src/test/resources/04007_G0010_2_2.mp3");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(testOutput.resolve("04007_G0010_2_2"));

        Path outputFile = service.ffmpegConvertToM4a(options);

        assertTrue(Files.exists(outputFile));
        assertEquals(testOutput.resolve("04007_G0010_2_2.m4a"), outputFile);
    }

    @Test
    public void testAudioAif() throws Exception {
        Path testFile = Path.of("src/test/resources/3AudioTrack.aiff");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(testOutput.resolve("3AudioTrack"));

        Path outputFile = service.ffmpegConvertToM4a(options);

        assertTrue(Files.exists(outputFile));
        assertEquals(testOutput.resolve("3AudioTrack.m4a"), outputFile);
    }

    @Test
    public void testAudioAu() throws Exception {
        Path testFile = Path.of("src/test/resources/b00310.au");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(testOutput.resolve("b00310"));

        Path outputFile = service.ffmpegConvertToM4a(options);

        assertTrue(Files.exists(outputFile));
        assertEquals(testOutput.resolve("b00310.m4a"), outputFile);
    }

    @Test
    public void testAudioWav() throws Exception {
        Path testFile = Path.of("src/test/resources/14.wav");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(testOutput.resolve("14"));

        Path outputFile = service.ffmpegConvertToM4a(options);

        assertTrue(Files.exists(outputFile));
        assertEquals(testOutput.resolve("14.m4a"), outputFile);
    }

    @Test
    public void testAudioWma() throws Exception {
        Path testFile = Path.of("src/test/resources/DS400038.WMA");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(testOutput.resolve("DS400038"));

        Path outputFile = service.ffmpegConvertToM4a(options);

        assertTrue(Files.exists(outputFile));
        assertEquals(testOutput.resolve("DS400038.m4a"), outputFile);
    }

    @Test
    public void testFileAlreadyExists() throws Exception {
        Files.createFile(testOutput.resolve("009.m4a"));
        Path testFile = Path.of("src/test/resources/009.mp4");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(testOutput.resolve("009"));

        var e = assertThrows(FileAlreadyExistsException.class, () -> {
            service.ffmpegConvertToM4a(options);
        });
        assertTrue(e.getMessage().contains("File already exists at "));
    }
}
