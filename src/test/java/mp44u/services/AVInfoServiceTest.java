package mp44u.services;

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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    public void testRetrieveAudioInfo() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path mockedInput = testOutput.resolve("test_input.mp3");
            Mp44uOptions options = new Mp44uOptions();
            options.setInputPath(mockedInput);
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList())).thenReturn("{\n" +
                    "[STREAM]\n" +
                    "codec_name=mp3\n" +
                    "codec_type=audio\n" +
                    "bit_rate=128000\n" +
                    "[/STREAM]");

            AVInfoService avInfoService = new AVInfoService();
            Map<String,String> testInfo = avInfoService.retrieveAudioVideoInfo(options);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("ffprobe", "-v", "quiet",
                            "-show_entries", "stream=codec_name,height,codec_type,bit_rate", mockedInput.toString()))));
            assertTrue(testInfo.containsKey("codec_name"));
            assertTrue(testInfo.containsValue("mp3"));
            assertTrue(testInfo.containsKey("bit_rate"));
            assertTrue(testInfo.containsValue("128000"));
        }
    }

    @Test
    public void testRetrieveVideoInfo() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path mockedInput = testOutput.resolve("test_input.mp4");
            Mp44uOptions options = new Mp44uOptions();
            options.setInputPath(mockedInput);
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList())).thenReturn("{\n" +
                    "[STREAM]\n" +
                    "codec_name=h264\n" +
                    "codec_type=video\n" +
                    "height=480\n" +
                    "bit_rate=923373\n" +
                    "[/STREAM]\n");

            AVInfoService avInfoService = new AVInfoService();
            Map<String,String> testInfo = avInfoService.retrieveAudioVideoInfo(options);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("ffprobe", "-v", "quiet",
                            "-show_entries", "stream=codec_name,height,codec_type,bit_rate", mockedInput.toString()))));
            assertTrue(testInfo.containsKey("codec_name"));
            assertTrue(testInfo.containsValue("h264"));
            assertTrue(testInfo.containsKey("height"));
            assertTrue(testInfo.containsValue("480"));
            assertTrue(testInfo.containsKey("bit_rate"));
            assertTrue(testInfo.containsValue("923373"));
        }
    }

    @Test
    public void testAudioEncodeOrCopy() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path mockedInput = testOutput.resolve("test_input.m4a");
            Mp44uOptions options = new Mp44uOptions();
            options.setInputPath(mockedInput);
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList())).thenReturn("{\n" +
                    "[STREAM]\n" +
                    "codec_name=aac\n" +
                    "codec_type=audio\n" +
                    "bit_rate=128000\n" +
                    "[/STREAM]");

            AVInfoService avInfoService = new AVInfoService();
            String encodeOrCopy = avInfoService.audioEncodeOrCopy(options);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("ffprobe", "-v", "quiet",
                            "-show_entries", "stream=codec_name,height,codec_type,bit_rate", mockedInput.toString()))));
            assertEquals(AVInfoService.AUDIO_COPY, encodeOrCopy);
        }
    }

    @Test
    public void testVideoEncodeOrCopy() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path mockedInput = testOutput.resolve("test_input.mp4");
            Mp44uOptions options = new Mp44uOptions();
            options.setInputPath(mockedInput);
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList())).thenReturn("{\n" +
                    "[STREAM]\n" +
                    "codec_name=h264\n" +
                    "codec_type=video\n" +
                    "height=480\n" +
                    "bit_rate=923373\n" +
                    "[/STREAM]\n");

            AVInfoService avInfoService = new AVInfoService();
            String encodeOrCopy = avInfoService.videoEncodeOrCopy(options);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    new ArrayList<>(Arrays.asList("ffprobe", "-v", "quiet",
                            "-show_entries", "stream=codec_name,height,codec_type,bit_rate", mockedInput.toString()))));
            assertEquals(AVInfoService.VIDEO_COPY, encodeOrCopy);
        }
    }

    @Test
    public void testRetrieveAudioAac() throws Exception {
        Path testFile = Path.of("src/test/resources/04007_G0010_2_2.aac");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        Map<String,String> testInfo = service.retrieveAudioVideoInfo(options);
        assertTrue(testInfo.containsKey("codec_name"));
        assertTrue(testInfo.containsValue("aac"));
        assertTrue(testInfo.containsKey("bit_rate"));
        assertTrue(testInfo.containsValue("128000"));
    }

    @Test
    public void testRetrieveVideoMp4() throws Exception {
        Path testFile = Path.of("src/test/resources/009.mp4");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        service.retrieveAudioVideoInfo(options);

        Map<String,String> testInfo = service.retrieveAudioVideoInfo(options);
        assertTrue(testInfo.containsKey("codec_name"));
        assertTrue(testInfo.containsValue("h264"));
        assertTrue(testInfo.containsKey("height"));
        assertTrue(testInfo.containsValue("480"));
        assertTrue(testInfo.containsKey("bit_rate"));
        assertTrue(testInfo.containsValue("923373"));
    }

    @Test
    public void testAudioEncodeOrCopyAac() throws Exception {
        Path testFile = Path.of("src/test/resources/04007_G0010_2_2.m4a");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        String encodeOrCopy = service.audioEncodeOrCopy(options);
        assertEquals(AVInfoService.AUDIO_COPY, encodeOrCopy);
    }

    @Test
    public void testAudioEncodeOrCopyMov() throws Exception {
        Path testFile = Path.of("src/test/resources/AMEN.MOV");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        String encodeOrCopy = service.audioEncodeOrCopy(options);
        assertEquals(AVInfoService.AUDIO_ENCODE, encodeOrCopy);
    }

    @Test
    public void testVideoEncodeOrCopyMp4() throws Exception {
        Path testFile = Path.of("src/test/resources/009.mp4");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        String encodeOrCopy = service.videoEncodeOrCopy(options);
        assertEquals(AVInfoService.VIDEO_COPY, encodeOrCopy);
    }

    @Test
    public void testVideoEncodeOrCopyMov() throws Exception {
        Path testFile = Path.of("src/test/resources/00288.MTS");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        String encodeOrCopy = service.videoEncodeOrCopy(options);
        assertEquals(AVInfoService.VIDEO_ENCODE, encodeOrCopy);
    }
}
