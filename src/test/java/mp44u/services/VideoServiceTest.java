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

public class VideoServiceTest {
    private AutoCloseable closeable;
    private VideoService service;

    @BeforeEach
    public void setup() throws Exception {
        closeable = openMocks(this);
        service = new VideoService();
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

    @Test
    public void testVideoMp4() throws Exception {
        Path testFile = Path.of("src/test/resources/009.mp4");
        Path outputFile = service.ffmpegConvertToMp4(testFile, Paths.get("src/test/resources/009"));

        assertTrue(Files.exists(outputFile));
        assertEquals(Path.of("src/test/resources/009_access.mp4"), outputFile);

        // delete file
        // ffmpeg is unable to create files in tmpdir unless tmpdir is mounted without noexec
        Files.delete(outputFile);
        assertFalse(Files.exists(outputFile));
    }

    @Test
    public void testVideoMov() throws Exception {
        Path testFile = Path.of("src/test/resources/AMEN.MOV");
        Path outputFile = service.ffmpegConvertToMp4(testFile, Paths.get("src/test/resources/AMEN"));

        assertTrue(Files.exists(outputFile));
        assertEquals(Path.of("src/test/resources/AMEN.mp4"), outputFile);

        // delete file
        Files.delete(outputFile);
        assertFalse(Files.exists(outputFile));
    }

    @Disabled("testFile is 886.2MB, too big to add to github")
    @Test
    public void testVideoMts() throws Exception {
        Path testFile = Path.of("src/test/resources/00026.MTS");
        Path outputFile = service.ffmpegConvertToMp4(testFile, Paths.get("src/test/resources/00026"));

        assertTrue(Files.exists(outputFile));
        assertEquals(Path.of("src/test/resources/00026.mp4"), outputFile);

        // delete file
        Files.delete(outputFile);
        assertFalse(Files.exists(outputFile));
    }
}
