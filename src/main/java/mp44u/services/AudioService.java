package mp44u.services;

import mp44u.util.CommandUtility;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.nio.file.Files;
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

    public void ffmpegConvertToM4a(Path inputPath, Path outputPath) throws Exception {
        try {
            String inputFile = inputPath.toString();
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
            String outputFile;
            String outputFilename = FilenameUtils.getBaseName(inputFile) + ".m4a";

            // if the output path is a directory
            if (Files.isDirectory(outputPath)) {
                outputFile = outputPath + "/" + outputFilename;
                // if the output path is a file
            } else if (Files.exists(outputPath.getParent())) {
                outputFile = outputPath + ".m4a";
            } else {
                throw new Exception(outputPath + " does not exist.");
            }

            List<String> command = new ArrayList<>(Arrays.asList(FFMPEG, input, inputFile, acodec, aacEncoder,
                    ba, bitrate, audioSampling, audioSamplingRate, y, nostdin, dither, triangular, outputFile));
            CommandUtility.executeCommand(command);

            //return Path.of(outputFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
