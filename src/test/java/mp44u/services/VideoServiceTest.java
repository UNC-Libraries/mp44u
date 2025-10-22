package mp44u.services;

import mp44u.options.Mp44uOptions;
import mp44u.util.CommandUtility;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
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
    public void testVideo() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path mockedInput = testOutput.resolve("test_input.mp4");
            Path mockedOutput = testOutput.resolve("test_output.mp4");
            Mp44uOptions options = new Mp44uOptions();
            options.setInputPath(mockedInput);
            options.setOutputPath(testOutput.resolve("test_output"));
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList())).thenReturn(mockedOutput.toString());

            VideoService videoService = new VideoService();
            videoService.ffmpegConvertToMp4(options);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("ffmpeg", "-i", mockedInput.toString(), "-map_chapters", "-1",
                            "-vf", "yadif=0:-1:1,scale=trunc(oh*dar/2)*2:min(ih\\,720)", "-vcodec", "libx264",
                            "-force_key_frames", "expr:gte(t,n_forced*2)", "-crf", "22", "-maxrate", "2M",
                            "-bufsize", "4M", "-pix_fmt", "yuv420p", "-acodec", "libfdk_aac", "-ab", "128k",
                            "-dither_method", "triangular", "-movflags", "faststart", mockedOutput.toString()))));
        }
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
