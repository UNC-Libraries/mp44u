package mp44u.services;

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

    /**
     * Run ffmpeg and convert audio file to m4a
     * @param options
     * @return path to m4a file
     */
    public Path ffmpegConvertToM4a(Mp44uOptions options) throws Exception {
        String inputFile = options.getInputPath().toString();
        String input = "-i";
        String acodec = "-acodec";
        String aacEncoder = "libfdk_aac";
        String ba = "-b:a";
        String bitrate = "128k";
        String audioSampling = "-ar";
        String audioSamplingRate = "44100";
        String y = "-y";
        String nostdin = "-nostdin";
        String dither = "-dither_method";
        String triangular = "triangular";
        Path outputPath = options.getOutputPath();
        String outputFilename = FilenameUtils.getBaseName(inputFile) + ".m4a";
        Path outputFile = FileService.buildOutputFile(outputPath, outputFilename, ".m4a");

        FileService.validateFiles(inputFile, outputFile);

        List<String> command = new ArrayList<>(Arrays.asList(FFMPEG, input, inputFile, acodec, aacEncoder,
                ba, bitrate, audioSampling, audioSamplingRate, y, nostdin, dither, triangular,
                outputFile.toString()));
        CommandUtility.executeCommand(command);

        return outputFile;
    }
}
