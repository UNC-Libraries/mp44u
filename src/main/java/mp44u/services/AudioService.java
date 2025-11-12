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
 * Service for transcoding audio files
 * @author krwong
 */
public class AudioService {
    private static final Logger log = getLogger(AudioService.class);

    private static final String FFMPEG = "ffmpeg";
    public static final List<String> AUDIO = Arrays.asList("-y", "-nostdin", "-dither_method", "triangular");
    public static final List<String> ENCODE = Arrays.asList("-acodec", "aac", "-ab", "128k", "-ar", "44100");
    public static final List<String> COPY = Arrays.asList("-c:a", "copy");

    private AVInfoService avInfoService;

    /**
     * Encode or copy audio file
     * @param options
     * @return path to m4a file
     */
    public Path convertOrCopyAudio(Mp44uOptions options) throws Exception {
        Path outputPath = null;

        EncodingOperation encodeOrCopy = avInfoService.getAudioEncodingOperation(options);
        if (encodeOrCopy.equals(EncodingOperation.ENCODE)) {
            outputPath = ffmpegConvertToM4a(options);
        } else if (encodeOrCopy.equals(EncodingOperation.COPY)) {
            outputPath = ffmpegCopyToM4a(options);
        }

        return outputPath;
    }

    /**
     * Run ffmpeg and convert audio file to m4a
     * @param options
     * @return path to m4a file
     */
    public Path ffmpegConvertToM4a(Mp44uOptions options) throws Exception {
        String inputFile = options.getInputPath().toString();
        String input = "-i";
        Path outputPath = options.getOutputPath();
        String outputFilename = FilenameUtils.getBaseName(inputFile) + ".m4a";
        Path outputFile = FileService.buildOutputFile(outputPath, outputFilename, ".m4a");

        FileService.validateFiles(inputFile, outputFile);

        List<String> command = new ArrayList<>(Arrays.asList(FFMPEG, input, inputFile, outputFile.toString()));
        command.addAll(ENCODE);
        command.addAll(AUDIO);

        CommandUtility.executeCommand(command);

        return outputFile;
    }

    /**
     * Run ffmpeg and copy audio file to m4a
     * @param options
     * @return path to m4a file
     */
    public Path ffmpegCopyToM4a(Mp44uOptions options) throws Exception {
        String inputFile = options.getInputPath().toString();
        String input = "-i";
        Path outputPath = options.getOutputPath();
        String outputFilename = FilenameUtils.getBaseName(inputFile) + ".m4a";
        Path outputFile = FileService.buildOutputFile(outputPath, outputFilename, ".m4a");

        FileService.validateFiles(inputFile, outputFile);

        List<String> command = new ArrayList<>(Arrays.asList(FFMPEG, input, inputFile, outputFile.toString()));
        command.addAll(COPY);
        command.addAll(AUDIO);

        CommandUtility.executeCommand(command);

        return outputFile;
    }

    public void setAvInfoService(AVInfoService avInfoService) {
        this.avInfoService = avInfoService;
    }
}
