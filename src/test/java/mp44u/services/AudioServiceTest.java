package mp44u.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.MockitoAnnotations.openMocks;

public class AudioServiceTest {
    private AutoCloseable closeable;
    private AudioService service;

    @BeforeEach
    public void setup() throws Exception {
        closeable = openMocks(this);
        service = new AudioService();
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

    @Test
    public void testAudioMp3() throws Exception {
        Path testFile = Path.of("src/test/resources/04007_G0010_2_2.mp3");
        Path outputFile = service.ffmpegConvertToM4a(testFile, Paths.get("src/test/resources/04007_G0010_2_2"));

        assertTrue(Files.exists(outputFile));
        assertEquals(Path.of("src/test/resources/04007_G0010_2_2.m4a"), outputFile);

        // delete file
        // ffmpeg is unable to create files in tmpdir unless it is mounted without noexec
        Files.delete(outputFile);
        assertFalse(Files.exists(outputFile));
    }

    @Disabled("testFile is 59MB, too big to add to github")
    @Test
    public void testAudioAif() throws Exception {
        Path testFile = Path.of("src/test/resources/VariationsOnScarboroughFair.aif");
        Path outputFile = service.ffmpegConvertToM4a(testFile, Path.of("src/test/resources/VariationsOnScarboroughFair"));

        assertTrue(Files.exists(outputFile));
        assertEquals(Path.of("src/test/resources/VariationsOnScarboroughFair.m4a"), outputFile);

        // delete file
        Files.delete(outputFile);
        assertFalse(Files.exists(outputFile));
    }

    @Test
    public void testAudioAu() throws Exception {
        Path testFile = Path.of("src/test/resources/b00310.au");
        Path outputFile = service.ffmpegConvertToM4a(testFile, Path.of("src/test/resources/b00310"));

        assertTrue(Files.exists(outputFile));
        assertEquals(Path.of("src/test/resources/b00310.m4a"), outputFile);

        // delete file
        Files.delete(outputFile);
        assertFalse(Files.exists(outputFile));
    }

    @Test
    public void testAudioWav() throws Exception {
        Path testFile = Path.of("src/test/resources/14.wav");
        Path outputFile = service.ffmpegConvertToM4a(testFile, Path.of("src/test/resources/14"));

        assertTrue(Files.exists(outputFile));
        assertEquals(Path.of("src/test/resources/14.m4a"), outputFile);

        // delete file
        Files.delete(outputFile);
        assertFalse(Files.exists(outputFile));
    }

    @Test
    public void testAudioWma() throws Exception {
        Path testFile = Path.of("src/test/resources/DS400038.WMA");
        Path outputFile = service.ffmpegConvertToM4a(testFile, Path.of("src/test/resources/DS400038"));

        assertTrue(Files.exists(outputFile));
        assertEquals(Path.of("src/test/resources/DS400038.m4a"), outputFile);

        // delete file
        Files.delete(outputFile);
        assertFalse(Files.exists(outputFile));
    }
}
