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

    public enum EncodingOperation { ENCODE, COPY; }

    /**
     * Run ffprobe and retrieve AV information for the input file
     * @param options
     * @return
      */
    private Map<String,String> retrieveAudioVideoInfo(Mp44uOptions options, String av) {
        // run ffprobe
        String inputFile = options.getInputPath().toString();
        String ffprobe = "ffprobe";
        String v = "-v";
        String quiet = "quiet";
        String selectStreams = "-select_streams";
        String selectedStreams = "";
        if (av.contains("audio")) {
            selectedStreams = "a:0";
        } else if (av.contains("video")) {
            selectedStreams = "v:0";
        }
        String showEntries = "-show_entries";
        String entries = "stream=codec_name,height,bit_rate";

        List<String> command = new ArrayList<>(Arrays.asList(ffprobe, v, quiet, selectStreams, selectedStreams,
                showEntries, entries, inputFile));
        String ffprobeOutput = CommandUtility.executeCommand(command);

        // split ffprobe output around \n and filter for values containing =
        Map<String,String> avInfo = new HashMap<>();
        List<String> streams = List.of(ffprobeOutput.split(System.lineSeparator()));
        List<String> streamInfo = streams.stream()
                .filter(str -> str.matches("[a-zA-z]*=[a-zA-z]*[0-9]*")).toList();

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
    public EncodingOperation getAudioEncodingOperation(Mp44uOptions options) {
        EncodingOperation audioEncoding = EncodingOperation.ENCODE;

        Map<String,String> avInfo = retrieveAudioVideoInfo(options, "audio");
        String codecName = "audio";
        if (avInfo.get("codec_name") != null) {
            codecName = avInfo.get("codec_name");
        }
        int bitRate = 192001;
        if (avInfo.get("bit_rate") != null) {
            bitRate = Integer.parseInt(avInfo.get("bit_rate"));
        }

        if (codecName.contains("aac") && bitRate <= 192000) {
            audioEncoding = EncodingOperation.COPY;
        }

        return audioEncoding;
    }

    /**
     * Use VIDEO_ENCODE if false and VIDEO_COPY if true for all video streams in file
     * codec_name: h264 AND height: <=720 AND bit_rate <= 3000000 (i.e., 3Mbps)
     * @param options
     * @return
     */
    public EncodingOperation getVideoEncodingOperation(Mp44uOptions options) {
        EncodingOperation videoEncoding = EncodingOperation.ENCODE;

        Map<String,String> avInfo = retrieveAudioVideoInfo(options, "video");
        String codecName = "video";
        if (avInfo.get("codec_name") != null) {
            codecName = avInfo.get("codec_name");
        }
        int height = 721;
        if (avInfo.get("height") != null) {
           height = Integer.parseInt(avInfo.get("height"));
        }
        int bitRate = 3000001;
        if (avInfo.get("bit_rate") != null) {
            bitRate = Integer.parseInt(avInfo.get("bit_rate"));
        }

        if (codecName.contains("h264") && height <= 720 && bitRate <= 3000000) {
            videoEncoding = EncodingOperation.COPY;
        }

        return videoEncoding;
    }
}
