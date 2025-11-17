package mp44u.services;

import mp44u.services.AVInfoService.EncodingOperation;
import mp44u.services.AVInfoService.Subtitles;
import mp44u.options.Mp44uOptions;
import mp44u.util.CommandUtility;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.MockitoAnnotations.openMocks;

public class AVInfoServiceTest {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private AutoCloseable closeable;
    private AVInfoService service;
    private Path testOutput = Paths.get("target/test_output");

    @BeforeEach
    public void setup() throws Exception {
        closeable = openMocks(this);
        System.setOut(new PrintStream(outputStreamCaptor));
        service = new AVInfoService();

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
    public void testGetAudioEncodingOperation() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path mockedInput = testOutput.resolve("test_input.m4a");
            Mp44uOptions options = new Mp44uOptions();
            options.setInputPath(mockedInput);
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList())).thenReturn(
                    "[STREAM]\n" +
                    "codec_name=aac\n" +
                    "bit_rate=128000\n" +
                    "[/STREAM]");

            AVInfoService avInfoService = new AVInfoService();
            AVInfoService.EncodingOperation encodeOrCopy = avInfoService.getAudioEncodingOperation(options);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("ffprobe", "-v", "quiet", "-select_streams", "a:0",
                            "-show_entries", "stream=codec_name,height,bit_rate,index:stream_tags=language",
                            mockedInput.toString()))));
            assertEquals(EncodingOperation.COPY, encodeOrCopy);
        }
    }

    @Test
    public void testGetVideoEncodingOperation() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path mockedInput = testOutput.resolve("test_input.mp4");
            Mp44uOptions options = new Mp44uOptions();
            options.setInputPath(mockedInput);
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList())).thenReturn(
                    "[STREAM]\n" +
                    "codec_name=h264\n" +
                    "height=480\n" +
                    "bit_rate=923373\n" +
                    "TAG:language=eng\n" +
                    "[/STREAM]\n");

            AVInfoService avInfoService = new AVInfoService();
            AVInfoService.EncodingOperation encodeOrCopy = avInfoService.getVideoEncodingOperation(options);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("ffprobe", "-v", "quiet", "-select_streams", "v:0",
                            "-show_entries", "stream=codec_name,height,bit_rate,index:stream_tags=language",
                            mockedInput.toString()))));
            assertEquals(EncodingOperation.COPY, encodeOrCopy);
        }
    }

    @Test
    public void testGetSubtitles() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path mockedInput = testOutput.resolve("test_input.mp4");
            Mp44uOptions options = new Mp44uOptions();
            options.setInputPath(mockedInput);
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList())).thenReturn(
                    "[STREAM]\n" +
                            "codec_name=h264\n" +
                            "height=480\n" +
                            "bit_rate=923373\n" +
                            "TAG:language=eng\n" +
                            "[/STREAM]\n");

            AVInfoService avInfoService = new AVInfoService();
            Subtitles subtitles = avInfoService.getSubtitles(options);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("ffprobe", "-v", "quiet", "-select_streams", "v:0",
                            "-show_entries", "stream=codec_name,height,bit_rate,index:stream_tags=language",
                            mockedInput.toString()))));
            assertEquals(Subtitles.SUBTITLES, subtitles);
        }
    }
}
