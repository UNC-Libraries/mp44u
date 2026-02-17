package mp44u.services;

import mp44u.errors.CommandException;
import mp44u.options.Mp44uOptions;
import mp44u.util.CommandUtility;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for retrieving audio/video information using ffprobe
 * @author krwong
 */
public class AVInfoService {
    private static final Logger log = getLogger(AVInfoService.class);

    public enum EncodingOperation { ENCODE, COPY; }
    public enum Subtitles { SUBTITLES, NO_SUBTITLES; }
    private static final int AUDIO_ENCODING_MIN_BIT_RATE = 192000;
    private static final int VIDEO_ENCODING_MIN_BIT_RATE = 3000000;
    private static final int VIDEO_ENCODING_MIN_HEIGHT = 720;

    /**
     * Run ffprobe and retrieve AV information for the input file
     * @param options
     * @return
      */
    public Map<String,String> retrieveAudioVideoInfo(Mp44uOptions options) {
        // run ffprobe
        String inputFile = options.getInputPath().toString();
        String ffprobe = "ffprobe";
        String v = "-v";
        String quiet = "quiet";
        String showEntries = "-show_entries";
        String entries = "stream=codec_type,codec_name,height,bit_rate,index:stream_tags=language";

        List<String> command = new ArrayList<>(Arrays.asList(ffprobe, v, quiet,
                showEntries, entries, inputFile));
        String ffprobeOutput = "";

        try {
            ffprobeOutput = CommandUtility.executeCommand(command);
        } catch (CommandException e) {
            log.warn("ffprobe command failed to execute for {}: {}", options.getInputPath(), command);
        }

        // split ffprobe output by [/STREAM] and use the codec_type to determine if stream contains audio/video info
        Map<String,String> avInfo = new HashMap<>();
        ArrayList<String> streams = new ArrayList<>(Arrays.asList(ffprobeOutput.split("\\[\\/STREAM\\]")));
        for (String stream : streams) {
            ArrayList<String> streamInfo = (ArrayList<String>) Arrays.stream(stream.split(System.lineSeparator()))
                    .filter(str -> str.matches(".+=.*")).collect(Collectors.toList());
            if (streamInfo.contains("codec_name=unknown")) {
                for (String streamValue : streamInfo) {
                    avInfo.put(streamValue.split("=")[0], streamValue.split("=")[1]);
                }
            }
            if (streamInfo.contains("codec_type=video")) {
                for (String streamValue : streamInfo) {
                    avInfo.put("video_" + streamValue.split("=")[0], streamValue.split("=")[1]);
                }
            }
            if (streamInfo.contains("codec_name=vc1")) {
                for (String streamValue : streamInfo) {
                    avInfo.put("video_" + streamValue.split("=")[0], streamValue.split("=")[1]);
                }
            }
            if (streamInfo.contains("codec_type=audio")) {
                for (String streamValue : streamInfo) {
                    avInfo.put("audio_" + streamValue.split("=")[0], streamValue.split("=")[1]);
                }
            }
        }

        return avInfo;
    }

    /**
     * Determine if the file's audio is encodable (has a non-zero bit_rate, no unknown codec_name)
     * @param avInfo
     * @return
     */
    public boolean audioEncodable(Map<String, String> avInfo) {
        boolean encodable = false;

        if ((avInfo.containsKey("audio_bit_rate") && avInfo.get("audio_bit_rate").matches("\\d+"))
                || (avInfo.containsKey("codec_name") && !avInfo.get("codec_name").matches("unknown"))) {
            encodable = true;
        }

        return encodable;
    }

    /**
     * Determine if the file's video is encodable (has a non-zero bit_rate, not vc1 video_codec_name,
     * no unknown codec_name)
     * @param avInfo
     * @return
     */
    public boolean videoEncodable(Map<String, String> avInfo) {
        boolean encodable = false;

        if ((avInfo.containsKey("video_bit_rate") && avInfo.get("video_bit_rate").matches("\\d+"))
                || (avInfo.containsKey("codec_name") && !avInfo.get("codec_name").matches("unknown"))) {
            if (avInfo.containsKey("video_codec_name") && !avInfo.get("video_codec_name").matches("vc1")) {
                encodable = true;
            }
        }

        return encodable;
    }

    /**
     * Use AUDIO_ENCODE if false and AUDIO_COPY if true for all audio streams in file
     * codec_name: aac AND bit_rate <= 192000 (i.e., 192kbps)
     * @param avInfo
     * @return
     */
    public EncodingOperation getAudioEncodingOperation(Map<String, String> avInfo) {
        EncodingOperation audioEncoding = EncodingOperation.ENCODE;

        String codecName = "audio";
        if (avInfo.get("audio_codec_name") != null) {
            codecName = avInfo.get("audio_codec_name");
        }

        int bitRate = AUDIO_ENCODING_MIN_BIT_RATE + 1;
        try {
            bitRate = Integer.parseInt(avInfo.get("audio_bit_rate"));
        } catch (NumberFormatException e) {
            log.warn("audio_bit_rate not found: {}", e.getMessage());
        }

        if (codecName.contains("aac") && bitRate <= AUDIO_ENCODING_MIN_BIT_RATE) {
            audioEncoding = EncodingOperation.COPY;
        }

        return audioEncoding;
    }

    /**
     * Use VIDEO_ENCODE if false and VIDEO_COPY if true for all video streams in file
     * codec_name: h264 AND height: <=720 AND bit_rate <= 3000000 (i.e., 3Mbps)
     * @param avInfo
     * @return
     */
    public EncodingOperation getVideoEncodingOperation(Map<String, String> avInfo) {
        EncodingOperation videoEncoding = EncodingOperation.ENCODE;

        String codecName = "video";
        if (avInfo.get("video_codec_name") != null) {
            codecName = avInfo.get("video_codec_name");
        }

        int height = VIDEO_ENCODING_MIN_HEIGHT + 1;
        try {
            height = Integer.parseInt(avInfo.get("video_height"));
        } catch (NumberFormatException e) {
            log.warn("video_height not found: {}", e.getMessage());
        }

        int bitRate = VIDEO_ENCODING_MIN_BIT_RATE + 1;
        try {
            bitRate = Integer.parseInt(avInfo.get("video_bit_rate"));
        } catch (NumberFormatException e) {
            log.warn("video_bit_rate not found: {}", e.getMessage());
        }

        if (codecName.contains("h264") && height <= VIDEO_ENCODING_MIN_HEIGHT
                && bitRate <= VIDEO_ENCODING_MIN_BIT_RATE) {
            videoEncoding = EncodingOperation.COPY;
        }

        return videoEncoding;
    }

    /**
     * Check for subtiltes in video file
     * @param avInfo
     * @return
     */
    public Subtitles getSubtitles(Map<String, String> avInfo) {
        Subtitles subtitles = Subtitles.NO_SUBTITLES;

        if (avInfo.get("video_TAG:language") != null) {
            subtitles = Subtitles.SUBTITLES;
        }
        return subtitles;
    }
}
