package mp44u;

import mp44u.services.AVInfoService.EncodingOperation;
import mp44u.services.AVInfoService.Subtitles;
import mp44u.options.Mp44uOptions;
import mp44u.services.AVInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

public class EncodingOperationIT {
    private static final Logger log = getLogger(EncodingOperationIT.class);

    private AVInfoService service;

    @BeforeEach
    public void setup() throws Exception {
        service = new AVInfoService();
    }

    @Test
    public void testGetAudioEncodableAac() throws Exception {
        Path testFile = Path.of("src/test/resources/3AudioTrack.aiff");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        Map<String,String> avInfo = service.retrieveAudioVideoInfo(options);
        boolean audioEncodable = service.audioEncodable(avInfo);
        assertTrue(audioEncodable);
    }

    @Test
    public void testGetVideoEncodableMov() throws Exception {
        Path testFile = Path.of("src/test/resources/AMEN.MOV");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        Map<String,String> avInfo = service.retrieveAudioVideoInfo(options);
        boolean audioEncodable = service.audioEncodable(avInfo);
        boolean videoEncodable = service.videoEncodable(avInfo);
        assertTrue(audioEncodable);
        assertTrue(videoEncodable);
    }

    @Test
    public void testGetVideoUnencodableMovUnknownCodecNameNoBitRate() throws Exception {
        Path testFile = Path.of("src/test/resources/IrvJoynerandScottHolme_h264_3000Kbps_720p.mov");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        Map<String,String> avInfo = service.retrieveAudioVideoInfo(options);
        boolean audioEncodable = service.audioEncodable(avInfo);
        boolean videoEncodable = service.videoEncodable(avInfo);
        assertFalse(audioEncodable);
        assertFalse(videoEncodable);
    }

    @Test
    public void testGetVideoUnencodableCodecType() throws Exception {
        Path testFile = Path.of("src/test/resources/IrvJoynerandScottHolmes-fullMPEG2_WMV_3000Kbps_720p.wmv");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        Map<String,String> avInfo = service.retrieveAudioVideoInfo(options);
        boolean audioEncodable = service.audioEncodable(avInfo);
        boolean videoEncodable = service.videoEncodable(avInfo);
        assertTrue(audioEncodable);
        assertFalse(videoEncodable);
    }

    @Test
    public void testGetVideoEncodableAudioUncodableMov() throws Exception {
        Path testFile = Path.of("src/test/resources/amen_noaudio.mov");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        Map<String,String> avInfo = service.retrieveAudioVideoInfo(options);
        boolean audioEncodable = service.audioEncodable(avInfo);
        boolean videoEncodable = service.videoEncodable(avInfo);
        assertFalse(audioEncodable);
        assertTrue(videoEncodable);
    }

    @Test
    public void testGetAudioEncodingOperationAac() throws Exception {
        Path testFile = Path.of("src/test/resources/04007_G0010_2_2.m4a");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        Map<String,String> avInfo = service.retrieveAudioVideoInfo(options);
        AVInfoService.EncodingOperation encodeOrCopy = service.getAudioEncodingOperation(avInfo);
        assertEquals(AVInfoService.EncodingOperation.COPY, encodeOrCopy);
    }

    @Test
    public void testGetVideoEncodingOperationMov() throws Exception {
        Path testFile = Path.of("src/test/resources/AMEN.MOV");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        Map<String,String> avInfo = service.retrieveAudioVideoInfo(options);
        EncodingOperation encodeOrCopy = service.getVideoEncodingOperation(avInfo);
        assertEquals(AVInfoService.EncodingOperation.ENCODE, encodeOrCopy);
    }

    @Test
    public void testGetAudioEncodingOperationMov() throws Exception {
        Path testFile = Path.of("src/test/resources/AMEN.MOV");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        Map<String,String> avInfo = service.retrieveAudioVideoInfo(options);
        EncodingOperation encodeOrCopy = service.getAudioEncodingOperation(avInfo);
        assertEquals(AVInfoService.EncodingOperation.ENCODE, encodeOrCopy);
    }

    @Test
    public void testGetVideoEncodingOperationNoAudio() throws Exception {
        Path testFile = Path.of("src/test/resources/amen_noaudio.mov");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        Map<String,String> avInfo = service.retrieveAudioVideoInfo(options);
        EncodingOperation encodeOrCopy = service.getVideoEncodingOperation(avInfo);
        assertEquals(AVInfoService.EncodingOperation.ENCODE, encodeOrCopy);
    }

    @Test
    public void testGetVideoEncodingOperationMp4() throws Exception {
        Path testFile = Path.of("src/test/resources/009.mp4");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        Map<String,String> avInfo = service.retrieveAudioVideoInfo(options);
        EncodingOperation encodeOrCopy = service.getVideoEncodingOperation(avInfo);
        assertEquals(EncodingOperation.COPY, encodeOrCopy);
    }

    @Test
    public void testGetVideoEncodingOperationMts() throws Exception {
        Path testFile = Path.of("src/test/resources/00288.MTS");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        Map<String,String> avInfo = service.retrieveAudioVideoInfo(options);
        EncodingOperation encodeOrCopy = service.getVideoEncodingOperation(avInfo);
        assertEquals(EncodingOperation.ENCODE, encodeOrCopy);
    }

    @Test
    public void testGetSubtitlesMp3() throws Exception {
        Path testFile = Path.of("src/test/resources/04007_G0010_2_2.mp3");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        Map<String,String> avInfo = service.retrieveAudioVideoInfo(options);
        Subtitles subtitles = service.getSubtitles(avInfo);
        assertEquals(Subtitles.NO_SUBTITLES, subtitles);
    }

    @Test
    public void testGetSubtitlesMp4() throws Exception {
        Path testFile = Path.of("src/test/resources/009.mp4");
        Mp44uOptions options = new Mp44uOptions();
        options.setInputPath(testFile);

        Map<String,String> avInfo = service.retrieveAudioVideoInfo(options);
        Subtitles subtitles = service.getSubtitles(avInfo);
        assertEquals(Subtitles.SUBTITLES, subtitles);
    }
}
