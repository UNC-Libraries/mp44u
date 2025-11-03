package mp44u.services;

import mp44u.options.Mp44uOptions;
import mp44u.util.CommandUtility;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for retrieving audio/video information using ffprobe
 * @author krwong
 */
public class AVInfoService {
    private static final Logger log = getLogger(AVInfoService.class);

    public static final String AUDIO_ENCODE = "AUDIO_ENCODE";
    public static final String AUDIO_COPY = "AUDIO_COPY";
    public static final String VIDEO_ENCODE = "VIDEO_ENCODE";
    public static final String VIDEO_COPY = "VIDEO_COPY";

    /**
     * Run ffprobe and retrieve AV information for the input file
     * @param options
     * @return
      */
    public Map<String,String> retrieveAudioVideoInfo(Mp44uOptions options) throws Exception {
        // run ffprobe
        String inputFile = options.getInputPath().toString();
        String ffprobe = "ffprobe";
        String v = "-v";
        String quiet = "quiet";
        String showEntries = "-show_entries";
        String entries = "stream=codec_name,height,codec_type,bit_rate";

        List<String> command = new ArrayList<>(Arrays.asList(ffprobe, v, quiet,
                showEntries, entries, inputFile));
        String ffprobeOutput = CommandUtility.executeCommand(command);

        // split ffprobe output around \n and filter for values containing =
        Map<String,String> avInfo = new HashMap<>();
        List<String> streams = List.of(ffprobeOutput.split(System.lineSeparator()));
        List<String> streamInfo = streams.stream()
                .filter(str -> str.matches("[a-zA-z]*=[a-zA-z]*[0-9]*")).limit(4).toList();

        for (String stream : streamInfo) {
            avInfo.put(stream.split("=")[0], stream.split("=")[1]);
        }

        return avInfo;
    }

    /**
     * Use AUDIO_ENCODE if false and AUDIO_COPY if true for all audio streams in file
     * codec_name: aac AND bit_rate <= 192000 (i.e., 192kbps)
     * @param options
     * @return
     */
    public String audioEncodeOrCopy(Mp44uOptions options) throws Exception {
        String audioEncoding = AUDIO_ENCODE;

        Map<String,String> avInfo = retrieveAudioVideoInfo(options);
        String codecName = avInfo.get("codec_name");
        int bitRate;
        if (avInfo.get("bit_rate") != null) {
            bitRate = Integer.parseInt(avInfo.get("bit_rate"));
        } else {
            bitRate = 192000;
        }

        if (codecName.contains("aac") && bitRate <= 192000) {
            audioEncoding = AUDIO_COPY;
        }

        return audioEncoding;
    }

    /**
     * Use VIDEO_ENCODE if false and VIDEO_COPY if true for all video streams in file
     * codec_name: h264 AND height: <=720 AND bit_rate <= 3000000 (i.e., 3Mbps)
     * @param options
     * @return
     */
    public String videoEncodeOrCopy(Mp44uOptions options) throws Exception {
        String videoEncoding = VIDEO_ENCODE;

        Map<String,String> avInfo = retrieveAudioVideoInfo(options);
        String codecName = avInfo.get("codec_name");
        int height = Integer.parseInt(avInfo.get("height"));
        int bitRate;
        if (avInfo.get("bit_rate") != null) {
            bitRate = Integer.parseInt(avInfo.get("bit_rate"));
        } else {
            bitRate = 3000000;
        }

        if (codecName.contains("h264") && height <= 720 && bitRate <= 3000000) {
            videoEncoding = VIDEO_COPY;
        }

        return videoEncoding;
    }
}
