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
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
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
    public void testGetAudioNotEncodable() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path mockedInput = testOutput.resolve("test_input.m4a");
            Mp44uOptions options = new Mp44uOptions();
            options.setInputPath(mockedInput);
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList(), anyInt())).thenReturn(
                    "[STREAM]\n" +
                        "index=0\n" +
                        "codec_name=unknown\n" +
                        "codec_type=audio\n" +
                        "channels=0\n" +
                        "channel_layout=unknown\n" +
                        "bit_rate=N/A\n" +
                        "TAG:language=eng\n" +
                        "[/STREAM]");

            AVInfoService avInfoService = new AVInfoService();
            Map<String,String> avInfo = avInfoService.retrieveAudioVideoInfo(options);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    Arrays.asList("ffprobe", "-v", "quiet",
                            "-show_entries", "stream=codec_type,codec_name,height,bit_rate,sample_aspect_ratio," +
                            "display_aspect_ratio,channels,channel_layout,index:stream_tags=language",
                            mockedInput.toString()), 0));

            var e = assertThrows(UnsupportedEncodingException.class, () -> {
                avInfoService.audioEncodable(avInfo);
            });
            assertTrue(e.getMessage().contains("Audio not encodable: unknown codec_name"));
        }
    }

    @Test
    public void testGetAudioVideoEncodable() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path mockedInput = testOutput.resolve("test_input.mp4");
            Mp44uOptions options = new Mp44uOptions();
            options.setInputPath(mockedInput);
            options.setSourceFormat("video/mp4");
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList(), anyInt())).thenReturn(
                    "[STREAM]\n" +
                        "index=0\n" +
                        "codec_name=h264\n" +
                        "codec_type=video\n" +
                        "height=480\n" +
                        "sample_aspect_ratio=8:9\n" +
                        "display_aspect_ratio=4:3\n" +
                        "bit_rate=923373\n" +
                        "TAG:language=eng\n" +
                        "[/STREAM]\n" +
                        "[STREAM]\n" +
                        "index=1\n" +
                        "codec_name=aac\n" +
                        "codec_type=audio\n" +
                        "channels=2\n" +
                        "channel_layout=stereo\n" +
                        "bit_rate=120192\n" +
                        "TAG:language=und\n" +
                        "[/STREAM]\n");

            AVInfoService avInfoService = new AVInfoService();
            Map<String,String> avInfo = avInfoService.retrieveAudioVideoInfo(options);
            boolean audioEncodable = avInfoService.audioEncodable(avInfo);
            boolean videoEncodable = avInfoService.videoEncodable(avInfo);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    Arrays.asList("ffprobe", "-v", "quiet",
                            "-show_entries", "stream=codec_type,codec_name,height,bit_rate,sample_aspect_ratio," +
                            "display_aspect_ratio,channels,channel_layout,index:stream_tags=language",
                            mockedInput.toString()), 0));
            assertTrue(audioEncodable);
            assertTrue(videoEncodable);
        }
    }

    @Test
    public void testGetVideoNotEncodable() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path mockedInput = testOutput.resolve("test_input.mp4");
            Mp44uOptions options = new Mp44uOptions();
            options.setInputPath(mockedInput);
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList(), anyInt())).thenReturn(
                    "[STREAM]\n" +
                        "index=0\n" +
                        "codec_name=wmapro\n" +
                        "codec_type=audio\n" +
                        "channels=2\n" +
                        "channel_layout=stereo\n" +
                        "bit_rate=160040\n" +
                        "[/STREAM]\n" +
                        "[STREAM]\n" +
                        "index=1\n" +
                        "codec_name=unknown\n" +
                        "codec_type=video\n" +
                        "height=720\n" +
                        "sample_aspect_ratio=N/A\n" +
                        "display_aspect_ratio=N/A\n" +
                        "bit_rate=2800000\n" +
                        "[/STREAM]\n");

            AVInfoService avInfoService = new AVInfoService();
            Map<String,String> avInfo = avInfoService.retrieveAudioVideoInfo(options);
            boolean audioEncodable = avInfoService.audioEncodable(avInfo);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    Arrays.asList("ffprobe", "-v", "quiet",
                            "-show_entries", "stream=codec_type,codec_name,height,bit_rate,sample_aspect_ratio," +
                            "display_aspect_ratio,channels,channel_layout,index:stream_tags=language",
                            mockedInput.toString()), 0));
            assertTrue(audioEncodable);

            var e = assertThrows(UnsupportedEncodingException.class, () -> {
                avInfoService.videoEncodable(avInfo);
            });
            assertTrue(e.getMessage().contains("Video not encodable: unknown codec_name"));
        }
    }

    @Test
    public void testGetVideoUnencodableUnknownAspectRatios() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path mockedInput = testOutput.resolve("test_input.mp4");
            Mp44uOptions options = new Mp44uOptions();
            options.setInputPath(mockedInput);
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList(), anyInt())).thenReturn(
                    "[STREAM]\n" +
                    "index=0\n" +
                    "codec_name=dvvideo\n" +
                    "codec_type=video\n" +
                    "height=480\n" +
                    "sample_aspect_ratio=N/A\n" +
                    "display_aspect_ratio=N/A\n" +
                    "bit_rate=28771200\n" +
                    "TAG:language=eng\n" +
                    "[/STREAM]\n" +
                    "[STREAM]\n" +
                    "index=1\n" +
                    "codec_name=pcm_s16le\n" +
                    "codec_type=audio\n" +
                    "channels=2\n" +
                    "channel_layout=unknown\n" +
                    "bit_rate=28771229\n" +
                    "TAG:language=eng\n" +
                    "[/STREAM]\n" +
                    "[STREAM]\n" +
                    "index=2\n" +
                    "codec_name=unknown\n" +
                    "codec_type=data\n" +
                    "bit_rate=2\n" +
                    "TAG:language=eng\n" +
                    "[/STREAM]");

            AVInfoService avInfoService = new AVInfoService();
            Map<String,String> avInfo = avInfoService.retrieveAudioVideoInfo(options);
            boolean audioEncodable = avInfoService.audioEncodable(avInfo);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    Arrays.asList("ffprobe", "-v", "quiet",
                            "-show_entries", "stream=codec_type,codec_name,height,bit_rate,sample_aspect_ratio," +
                            "display_aspect_ratio,channels,channel_layout,index:stream_tags=language",
                            mockedInput.toString()), 0));
            assertTrue(audioEncodable);

            var e = assertThrows(UnsupportedEncodingException.class, () -> {
                avInfoService.videoEncodable(avInfo);
            });
            assertTrue(e.getMessage().contains("Video not encodable: unknown aspect_ratio"));
        }
    }

    @Test
    public void testGetAudioEncodingOperation() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path mockedInput = testOutput.resolve("test_input.m4a");
            Mp44uOptions options = new Mp44uOptions();
            options.setInputPath(mockedInput);
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList(), anyInt())).thenReturn(
                    "[STREAM]\n" +
                    "index=0\n" +
                    "codec_name=aac\n" +
                    "codec_type=audio\n" +
                    "channels=2\n" +
                    "channel_layout=stereo\n" +
                    "bit_rate=128000\n" +
                    "TAG:language=und\n" +
                    "[/STREAM]\n");

            AVInfoService avInfoService = new AVInfoService();
            Map<String,String> avInfo = avInfoService.retrieveAudioVideoInfo(options);
            AVInfoService.EncodingOperation encodeOrCopy = avInfoService.getAudioEncodingOperation(avInfo);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    Arrays.asList("ffprobe", "-v", "quiet",
                            "-show_entries", "stream=codec_type,codec_name,height,bit_rate,sample_aspect_ratio," +
                            "display_aspect_ratio,channels,channel_layout,index:stream_tags=language",
                            mockedInput.toString()), 0));
            assertEquals(EncodingOperation.COPY, encodeOrCopy);
        }
    }

    @Test
    public void testGetVideoEncodingOperation() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path mockedInput = testOutput.resolve("test_input.mp4");
            Mp44uOptions options = new Mp44uOptions();
            options.setInputPath(mockedInput);
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList(), anyInt())).thenReturn(
                    "[STREAM]\n" +
                    "index=0\n" +
                    "codec_name=h264\n" +
                    "codec_type=video\n" +
                    "height=480\n" +
                    "sample_aspect_ratio=8:9\n" +
                    "display_aspect_ratio=4:3\n" +
                    "bit_rate=923373\n" +
                    "TAG:language=und\n" +
                    "[/STREAM]\n" +
                    "[STREAM]\n" +
                    "index=1\n" +
                    "codec_name=aac\n" +
                    "codec_type=audio\n" +
                    "channels=2\n" +
                    "channel_layout=stereo\n" +
                    "bit_rate=120192\n" +
                    "TAG:language=und\n" +
                    "[/STREAM]\n");

            AVInfoService avInfoService = new AVInfoService();
            Map<String,String> avInfo = avInfoService.retrieveAudioVideoInfo(options);
            AVInfoService.EncodingOperation encodeOrCopy = avInfoService.getVideoEncodingOperation(avInfo);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    Arrays.asList("ffprobe", "-v", "quiet",
                            "-show_entries", "stream=codec_type,codec_name,height,bit_rate,sample_aspect_ratio," +
                            "display_aspect_ratio,channels,channel_layout,index:stream_tags=language",
                            mockedInput.toString()), 0));
            assertEquals(EncodingOperation.COPY, encodeOrCopy);
        }
    }

    @Test
    public void testGetVideoEncodingOperationAudioFirst() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path mockedInput = testOutput.resolve("test_input.mp4");
            Mp44uOptions options = new Mp44uOptions();
            options.setInputPath(mockedInput);
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList(), anyInt())).thenReturn(
                    "[STREAM]\n" +
                    "index=0\n" +
                    "codec_name=aac\n" +
                    "codec_type=audio\n" +
                    "channels=2\n" +
                    "channel_layout=stereo\n" +
                    "bit_rate=120192\n" +
                    "TAG:language=und\n" +
                    "[/STREAM]\n" +
                    "[STREAM]\n" +
                    "index=1\n" +
                    "codec_name=h264\n" +
                    "codec_type=video\n" +
                    "height=480\n" +
                    "sample_aspect_ratio=8:9\n" +
                    "display_aspect_ratio=4:3\n" +
                    "bit_rate=923373\n" +
                    "TAG:language=und\n" +
                    "[/STREAM]\n");

            AVInfoService avInfoService = new AVInfoService();
            Map<String,String> avInfo = avInfoService.retrieveAudioVideoInfo(options);
            AVInfoService.EncodingOperation encodeOrCopy = avInfoService.getVideoEncodingOperation(avInfo);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    Arrays.asList("ffprobe", "-v", "quiet",
                            "-show_entries", "stream=codec_type,codec_name,height,bit_rate,sample_aspect_ratio," +
                            "display_aspect_ratio,channels,channel_layout,index:stream_tags=language",
                            mockedInput.toString()), 0));
            assertEquals(EncodingOperation.COPY, encodeOrCopy);
        }
    }

    @Test
    public void testGetVideoEncodingOperationNoAudio() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path mockedInput = testOutput.resolve("test_input.mp4");
            Mp44uOptions options = new Mp44uOptions();
            options.setInputPath(mockedInput);
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList(), anyInt())).thenReturn(
                    "[STREAM]\n" +
                    "index=0\n" +
                    "codec_name=h264\n" +
                    "codec_type=video\n" +
                    "height=480\n" +
                    "sample_aspect_ratio=73728:75913\n" +
                    "display_aspect_ratio=98304:75913\n" +
                    "bit_rate=923373\n" +
                    "TAG:language=eng\n" +
                    "[SIDE_DATA]\n" +
                    "[/SIDE_DATA]\n" +
                    "[/STREAM]\n");

            AVInfoService avInfoService = new AVInfoService();
            Map<String,String> avInfo = avInfoService.retrieveAudioVideoInfo(options);
            AVInfoService.EncodingOperation encodeOrCopy = avInfoService.getVideoEncodingOperation(avInfo);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    Arrays.asList("ffprobe", "-v", "quiet",
                            "-show_entries", "stream=codec_type,codec_name,height,bit_rate,sample_aspect_ratio," +
                            "display_aspect_ratio,channels,channel_layout,index:stream_tags=language",
                            mockedInput.toString()), 0));
            assertEquals(EncodingOperation.COPY, encodeOrCopy);
        }
    }

    @Test
    public void testGetSubtitles() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path mockedInput = testOutput.resolve("test_input.mp4");
            Mp44uOptions options = new Mp44uOptions();
            options.setInputPath(mockedInput);
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList(), anyInt())).thenReturn(
                    "[STREAM]\n" +
                        "index=0\n" +
                        "codec_name=h264\n" +
                        "codec_type=video\n" +
                        "height=480\n" +
                        "sample_aspect_ratio=8:9\n" +
                        "display_aspect_ratio=4:3\n" +
                        "bit_rate=923373\n" +
                        "TAG:language=und\n" +
                        "[/STREAM]\n" +
                        "[STREAM]\n" +
                        "index=1\n" +
                        "codec_name=aac\n" +
                        "codec_type=audio\n" +
                        "channels=2\n" +
                        "channel_layout=stereo\n" +
                        "bit_rate=120192\n" +
                        "TAG:language=und\n" +
                        "[/STREAM]\n");

            AVInfoService avInfoService = new AVInfoService();
            Map<String,String> avInfo = avInfoService.retrieveAudioVideoInfo(options);
            Subtitles subtitles = avInfoService.getSubtitles(avInfo);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    Arrays.asList("ffprobe", "-v", "quiet",
                            "-show_entries", "stream=codec_type,codec_name,height,bit_rate,sample_aspect_ratio," +
                            "display_aspect_ratio,channels,channel_layout,index:stream_tags=language",
                            mockedInput.toString()), 0));
            assertEquals(Subtitles.SUBTITLES, subtitles);
        }
    }

    @Test
    public void testMonoAudio() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path mockedInput = testOutput.resolve("test_input.mov");
            Mp44uOptions options = new Mp44uOptions();
            options.setInputPath(mockedInput);
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList(), anyInt())).thenReturn(
                    "[STREAM]\n" +
                        "index=0\n" +
                        "codec_name=h264\n" +
                        "codec_type=video\n" +
                        "height=480\n" +
                        "sample_aspect_ratio=8:9\n" +
                        "display_aspect_ratio=4:3\n" +
                        "bit_rate=923373\n" +
                        "TAG:language=und\n" +
                        "[/STREAM]\n" +
                        "[STREAM]\n" +
                        "index=1\n" +
                        "codec_name=aac\n" +
                        "codec_type=audio\n" +
                        "channels=1\n" +
                        "channel_layout=stereo\n" +
                        "bit_rate=120192\n" +
                        "TAG:language=und\n" +
                        "[/STREAM]\n");

            AVInfoService avInfoService = new AVInfoService();
            Map<String,String> avInfo = avInfoService.retrieveAudioVideoInfo(options);
            boolean monoAudio = avInfoService.monoAudio(avInfo);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    Arrays.asList("ffprobe", "-v", "quiet",
                            "-show_entries", "stream=codec_type,codec_name,height,bit_rate,sample_aspect_ratio," +
                                    "display_aspect_ratio,channels,channel_layout,index:stream_tags=language",
                            mockedInput.toString()), 0));
            assertTrue(monoAudio);
        }
    }
}
