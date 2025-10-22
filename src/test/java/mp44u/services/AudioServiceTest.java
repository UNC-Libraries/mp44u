package mp44u.services;

import mp44u.options.Mp44uOptions;
import mp44u.util.CommandUtility;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
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
    public void testAudio() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path mockedInput = testOutput.resolve("test_input.mp3");
            Path mockedOutput = testOutput.resolve("test_output.m4a");
            Mp44uOptions options = new Mp44uOptions();
            options.setInputPath(mockedInput);
            options.setOutputPath(testOutput.resolve("test_output"));
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList())).thenReturn(mockedOutput.toString());

            AudioService audioService = new AudioService();
            audioService.ffmpegConvertToM4a(options);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("ffmpeg", "-i", mockedInput.toString(), "-acodec", "libfdk_aac",
                            "-b:a", "128k", "-ar", "44100", "-y", "-nostdin", "-dither_method", "triangular",
                            mockedOutput.toString()))));
        }
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
