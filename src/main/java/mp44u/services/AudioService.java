package mp44u.services;

import mp44u.services.AVInfoService.EncodingOperation;
import mp44u.options.Mp44uOptions;
import mp44u.util.CommandUtility;
import mp44u.util.FileService;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for transcoding audio files
 * @author krwong
 */
public class AudioService {
    private static final Logger log = getLogger(AudioService.class);

    private static final String FFMPEG = "ffmpeg";
    public static final List<String> ENCODE = Arrays.asList("-acodec", "aac", "-ab", "128k", "-ar", "44100",
            "-y", "-nostdin", "-dither_method", "triangular");
    public static final List<String> COPY = Arrays.asList("-c:a", "copy");
    public static final String THREADS = "-threads";

    private AVInfoService avInfoService;

    /**
     * Encode or copy audio file
     *
     * @param options
     * @return
     */
    public void convertOrCopyAudio(Mp44uOptions options) throws Exception {
        if (Files.notExists(options.getInputPath())) {
            throw new NoSuchFileException(options.getInputPath().toString());
        }
        Map<String,String> avInfo = avInfoService.retrieveAudioVideoInfo(options);
        EncodingOperation audioEncodingOperation = avInfoService.getAudioEncodingOperation(avInfo);

        if (avInfoService.audioEncodable(avInfo)) {
            ffmpegEncodeToM4a(options, audioEncodingOperation);
        } else {
            log.warn("{} not encodable, no audio_bit_rate found", options.getInputPath());
        }

    }

    /**
     * Run ffmpeg and convert/copy audio file to m4a
     * @param options
     * @return path to m4a file
     */
    public Path ffmpegEncodeToM4a(Mp44uOptions options, EncodingOperation audioEncodingOperation) throws Exception {
        String inputFile = options.getInputPath().toString();
        String input = "-i";
        Path outputPath = options.getOutputPath();
        String outputFilename = FilenameUtils.getBaseName(inputFile);
        Path outputFile = FileService.buildOutputFile(outputPath, outputFilename, ".m4a");

        FileService.validateFiles(inputFile, outputFile);

        List<String> command = new ArrayList<>(Arrays.asList(FFMPEG, input, inputFile));
        // encode or copy audio
        if (audioEncodingOperation.equals(EncodingOperation.ENCODE)) {
            command.addAll(ENCODE);
            command.add(THREADS);
            command.add(String.valueOf(options.getThreads()));
        } else {
            command.addAll(COPY);
        }

        command.add(outputFile.toString());
        CommandUtility.executeCommand(command);

        return outputFile;
    }

    public void setAvInfoService(AVInfoService avInfoService) {
        this.avInfoService = avInfoService;
    }
}
