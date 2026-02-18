package mp44u.services;

import mp44u.services.AVInfoService.EncodingOperation;
import mp44u.services.AVInfoService.Subtitles;
import mp44u.options.Mp44uOptions;
import mp44u.util.CommandUtility;
import mp44u.util.FileService;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for transcoding video files
 * @author krwong
 */
public class VideoService {
    private static final Logger log = getLogger(VideoService.class);

    private static final String FFMPEG = "ffmpeg";
    private static final String NO_STATS = "-nostats";
    private static final String NO_STDIN = "-nostdin";
    public static final List<String> VIDEO = Arrays.asList("-map_chapters", "-1", "-movflags", "faststart");
    public static final List<String> SUBTITLES = Arrays.asList("-c:s", "mov_text");
    public static final List<String> ENCODE = Arrays.asList("-vcodec", "libx264", "-crf", "22",
            "-vf", "yadif=0:-1:1,scale=trunc(oh*dar/2)*2:min(ih\\,720)",
            "-force_key_frames", "expr:gte(t,n_forced*2)", "-maxrate", "2M", "-bufsize", "4M", "-pix_fmt", "yuv420p");
    public static final List<String> ENCODE_SWF = Arrays.asList("-vcodec", "libx264", "-crf", "22",
            "-vf", "yadif=0:-1:1,scale=trunc(oh*dar/2)*2:trunc(min(ih\\,720)/2)*2",
            "-force_key_frames", "expr:gte(t,n_forced*2)", "-maxrate", "2M", "-bufsize", "4M", "-pix_fmt", "yuv420p");
    public static final List<String> COPY = Arrays.asList("-c:v", "copy");
    public static final String SKIP_AUDIO = "-an";
    public static final String THREADS = "-threads";

    private AVInfoService avInfoService;

    /**
     * Encode or copy video file
     * @param options
     */
    public void convertOrCopyVideo(Mp44uOptions options) throws Exception {
        if (Files.notExists(options.getInputPath())) {
            throw new NoSuchFileException(options.getInputPath().toString());
        }
        Map<String,String> avInfo = avInfoService.retrieveAudioVideoInfo(options);
        boolean videoEncodable = avInfoService.videoEncodable(avInfo);

        if (videoEncodable) {
            EncodingOperation videoEncodingOperation = avInfoService.getVideoEncodingOperation(avInfo);
            EncodingOperation audioEncodingOperation = avInfoService.getAudioEncodingOperation(avInfo);
            Subtitles subtitles = avInfoService.getSubtitles(avInfo);
            boolean audioEncodable = avInfoService.audioEncodable(avInfo);
            int numberChannels = Integer.parseInt(avInfo.get("audio_channels"));
            ffmpegEncodeToMp4(options, videoEncodingOperation, audioEncodingOperation, subtitles,
                    audioEncodable, numberChannels);
        } else {
            throw new UnsupportedEncodingException(options.getInputPath() + " not encodable: "
                + "unknown codec_name, unknown aspect_ratio, or codec_name=vc_1");
        }

    }

    /**
     * Run ffmpeg and convert/copy video file to mp4
     * @param options
     * @return path to mp4 file
     */
    public Path ffmpegEncodeToMp4(Mp44uOptions options, EncodingOperation videoEncodingOperation,
                                  EncodingOperation audioEncodingOperation, Subtitles subtitles,
                                  boolean audioEncodable, int numberChannels) throws Exception {
        String inputFile = options.getInputPath().toString();
        String input = "-i";
        Path outputPath = options.getOutputPath();
        String outputFilename = FilenameUtils.getBaseName(inputFile);
        Path outputFile = FileService.buildOutputFile(outputPath, outputFilename, ".mp4");

        FileService.validateFiles(inputFile, outputFile);

        List<String> command = new ArrayList<>(Arrays.asList(FFMPEG, NO_STDIN, NO_STATS, input, inputFile));
        command.addAll(VIDEO);


        // get subtitles
        if (subtitles.equals(Subtitles.SUBTITLES)) {
            command.addAll(SUBTITLES);
        }

        // encode or copy video
        if (videoEncodingOperation.equals(EncodingOperation.ENCODE)) {
            // use different encoder if SWF file
            if (options.getSourceFormat().matches("application/x-shockwave-flash")) {
                command.addAll(ENCODE_SWF);
            } else{
                command.addAll(ENCODE);
            }
            command.add(THREADS);
            command.add(String.valueOf(options.getThreads()));
        } else {
            command.addAll(COPY);
        }

        // if audio exists, encode or copy audio
        if (audioEncodable) {
            if (audioEncodingOperation.equals(EncodingOperation.ENCODE)) {
                command.addAll(AudioService.ENCODE);
                command.add(THREADS);
                command.add(String.valueOf(options.getThreads()));
            } else {
                command.addAll(AudioService.COPY);
            }

            // for audio_channel = 1, add -ac 2 to prevent encoding error
            if (numberChannels == 1) {
                command.addAll(AudioService.CHANNELS);
            }
        } else {
            command.add(SKIP_AUDIO);
        }

        command.add(outputFile.toString());
        log.debug("Running video command: {}", String.join(" ", command));
        CommandUtility.executeCommand(command, options.getSubcommandTimeout());

        return outputFile;
    }

    public void setAvInfoService(AVInfoService avInfoService) {
        this.avInfoService = avInfoService;
    }
}
