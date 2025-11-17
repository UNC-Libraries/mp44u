package mp44u;

import mp44u.services.AVInfoService.EncodingOperation;
import mp44u.services.AVInfoService.Subtitles;
import mp44u.options.Mp44uOptions;
import mp44u.services.AVInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.slf4j.LoggerFactory.getLogger;

public class EncodingOperationIT {
    private static final Logger log = getLogger(EncodingOperationIT.class);

    private AVInfoService service;

    @BeforeEach
    public void setup() throws Exception {
        service = new AVInfoService();
    }

    @Test
    public void testGetAudioEncodingOperationAac() throws Exception {
        Path testFile = Path.of("src/test/resources/04007_G0010_2_2.m4a");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        AVInfoService.EncodingOperation encodeOrCopy = service.getAudioEncodingOperation(options);
        assertEquals(AVInfoService.EncodingOperation.COPY, encodeOrCopy);
    }

    @Test
    public void testGetVideoEncodingOperationMov() throws Exception {
        Path testFile = Path.of("src/test/resources/AMEN.MOV");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        EncodingOperation encodeOrCopy = service.getVideoEncodingOperation(options);
        assertEquals(AVInfoService.EncodingOperation.ENCODE, encodeOrCopy);
    }

    @Test
    public void testGetAudioEncodingOperationMov() throws Exception {
        Path testFile = Path.of("src/test/resources/AMEN.MOV");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        EncodingOperation encodeOrCopy = service.getAudioEncodingOperation(options);
        assertEquals(AVInfoService.EncodingOperation.ENCODE, encodeOrCopy);
    }

    @Test
    public void testGetVideoEncodingOperationMp4() throws Exception {
        Path testFile = Path.of("src/test/resources/009.mp4");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        EncodingOperation encodeOrCopy = service.getVideoEncodingOperation(options);
        assertEquals(EncodingOperation.COPY, encodeOrCopy);
    }

    @Test
    public void testGetVideoEncodingOperationMts() throws Exception {
        Path testFile = Path.of("src/test/resources/00288.MTS");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        EncodingOperation encodeOrCopy = service.getVideoEncodingOperation(options);
        assertEquals(EncodingOperation.ENCODE, encodeOrCopy);
    }

    @Test
    public void testGetSubtitlesMp3() throws Exception {
        Path testFile = Path.of("src/test/resources/04007_G0010_2_2.mp3");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        Subtitles subtitles = service.getSubtitles(options);
        assertEquals(Subtitles.NO_SUBTITLES, subtitles);
    }

    @Test
    public void testGetSubtitlesMp4() throws Exception {
        Path testFile = Path.of("src/test/resources/009.mp4");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        Subtitles subtitles = service.getSubtitles(options);
        assertEquals(Subtitles.SUBTITLES, subtitles);
    }
}
