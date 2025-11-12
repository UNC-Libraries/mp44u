package mp44u.services;

import mp44u.services.AVInfoService.EncodingOperation;
import mp44u.options.Mp44uOptions;
import mp44u.util.CommandUtility;
import mp44u.util.FileService;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for transcoding video files
 * @author krwong
 */
public class VideoService {
    private static final Logger log = getLogger(VideoService.class);

    private static final String FFMPEG = "ffmpeg";
    public static final List<String> VIDEO = Arrays.asList("-map_chapters", "-1", "-movflags", "faststart");
    public static final List<String> SUBTITLES = Arrays.asList("-c:s", "mov_text");
    public static final List<String> ENCODE = Arrays.asList("-vcodec", "libx264", "-crf", "22",
            "-vf", "yadif=0:-1:1,scale=trunc(oh*dar/2)*2:min(ih\\,720)",
            "-force_key_frames", "expr:gte(t,n_forced*2)", "-maxrate", "2M", "-bufsize", "4M", "-pix_fmt", "yuv420p");
    public static final List<String> COPY = Arrays.asList("-c:v", "copy");

    private AVInfoService avInfoService;

    /**
     * Encode or copy video file
     * @param options
     * @return path to mp4 file
     */
    public Path convertOrCopyVideo(Mp44uOptions options) throws Exception {
        Path outputPath = null;

        EncodingOperation videoEncodingOperation = avInfoService.getVideoEncodingOperation(options);
        EncodingOperation audioEncodingOperation = avInfoService.getAudioEncodingOperation(options);
        if (videoEncodingOperation.equals(EncodingOperation.ENCODE)) {
            outputPath = ffmpegConvertToMp4(options, audioEncodingOperation);
        } else if (videoEncodingOperation.equals(EncodingOperation.COPY)) {
            outputPath = ffmpegCopyToMp4(options, audioEncodingOperation);
        }

        return outputPath;
    }

    /**
     * Run ffmpeg and convert video file to mp4
     * @param options
     * @return path to mp4 file
     */
    public Path ffmpegConvertToMp4(Mp44uOptions options, EncodingOperation audioEncodingOperation) throws Exception {
        String inputFile = options.getInputPath().toString();
        String input = "-i";
        Path outputPath = options.getOutputPath();
        String outputFilename = FilenameUtils.getBaseName(inputFile) + ".mp4";
        Path outputFile = FileService.buildOutputFile(outputPath, outputFilename, ".mp4");

        FileService.validateFiles(inputFile, outputFile);

        List<String> command = new ArrayList<>(Arrays.asList(FFMPEG, input, inputFile, outputFile.toString()));
        command.addAll(VIDEO);
        command.addAll(SUBTITLES);
        command.addAll(ENCODE);

        if (audioEncodingOperation.equals(EncodingOperation.COPY)) {
            command.addAll(AudioService.COPY);
        } else {
            command.addAll(AudioService.ENCODE);
        }
        command.addAll(AudioService.AUDIO);

        CommandUtility.executeCommand(command);

        return outputFile;
    }

    /**
     * Run ffmpeg and copy video file to mp4
     * @param options
     * @return path to mp4 file
     */
    public Path ffmpegCopyToMp4(Mp44uOptions options, EncodingOperation audioEncodingOperation) throws Exception {
        String inputFile = options.getInputPath().toString();
        String input = "-i";
        Path outputPath = options.getOutputPath();
        String outputFilename = FilenameUtils.getBaseName(inputFile) + ".mp4";
        Path outputFile = FileService.buildOutputFile(outputPath, outputFilename, ".mp4");

        FileService.validateFiles(inputFile, outputFile);

        List<String> command = new ArrayList<>(Arrays.asList(FFMPEG, input, inputFile, outputFile.toString()));
        command.addAll(VIDEO);
        command.addAll(SUBTITLES);
        command.addAll(COPY);
        
        if (audioEncodingOperation.equals(EncodingOperation.COPY)) {
            command.addAll(AudioService.COPY);
        } else {
            command.addAll(AudioService.ENCODE);
        }
        command.addAll(AudioService.AUDIO);

        CommandUtility.executeCommand(command);

        return outputFile;
    }

    public void setAvInfoService(AVInfoService avInfoService) {
        this.avInfoService = avInfoService;
    }
}
