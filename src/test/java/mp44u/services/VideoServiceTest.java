package mp44u.services;

import mp44u.options.Mp44uOptions;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.MockitoAnnotations.openMocks;

public class VideoServiceTest {
    private AutoCloseable closeable;
    private VideoService service;
    private Path testOutput = Paths.get("target/test_output");

    @BeforeEach
    public void setup() throws Exception {
        closeable = openMocks(this);
        service = new VideoService();

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
    public void testVideoMp4() throws Exception {
        Path testFile = Path.of("src/test/resources/009.mp4");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(testOutput.resolve("009_access"));

        Path outputFile = service.ffmpegConvertToMp4(options);

        assertTrue(Files.exists(outputFile));
        assertEquals(testOutput.resolve("009_access.mp4"), outputFile);
    }

    @Test
    public void testVideoMov() throws Exception {
        Path testFile = Path.of("src/test/resources/AMEN.MOV");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(testOutput.resolve("AMEN"));

        Path outputFile = service.ffmpegConvertToMp4(options);

        assertTrue(Files.exists(outputFile));
        assertEquals(testOutput.resolve("AMEN.mp4"), outputFile);
    }

    @Test
    public void testVideoMts() throws Exception {
        Path testFile = Path.of("src/test/resources/00288.MTS");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(testOutput.resolve("00288"));

        Path outputFile = service.ffmpegConvertToMp4(options);

        assertTrue(Files.exists(outputFile));
        assertEquals(testOutput.resolve("00288.mp4"), outputFile);
    }

    @Test
    public void testSameInputOutputFile() throws Exception {
        Path testFile = Path.of("src/test/resources/009.mp4");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(Paths.get("src/test/resources/009"));

        var e = assertThrows(IllegalArgumentException.class, () -> {
            service.ffmpegConvertToMp4(options);
        });
        assertTrue(e.getMessage().contains("Input and output paths cannot be the same"));
    }
}
