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
 * Service for transcoding video files
 * @author krwong
 */
public class VideoService {
    private static final Logger log = getLogger(VideoService.class);

    private static final String FFMPEG = "ffmpeg";

    /**
     * Run ffmpeg and convert video file to mp4
     * @param options
     * @return path to mp4 file
     */
    public Path ffmpegConvertToMp4(Mp44uOptions options) throws Exception {
        String inputFile = options.getInputPath().toString();
        String input = "-i";
        String mapChapters = "-map_chapters";
        String mapChaptersValue = "-1";
        String vf = "-vf";
        String vfValue = "yadif=0:-1:1,scale=trunc(oh*dar/2)*2:min(ih\\,720)";
        String vcodec = "-vcodec";
        String vcodecValue = "libx264";
        String forceKeyFrames = "-force_key_frames";
        String expr = "expr:gte(t,n_forced*2)";
        String crf = "-crf";
        String crfValue = "22";
        String maxrate = "-maxrate";
        String maxrateValue = "2M";
        String bufSize = "-bufsize";
        String bufSizeValue = "4M";
        String pixFmt = "-pix_fmt";
        String pixFmtValue = "yuv420p";
        String acodec = "-acodec";
        String acodecValue = "libfdk_aac";
        String ab = "-ab";
        String abValue = "128k";
        String ditherMethod = "-dither_method";
        String ditherMethodValue = "triangular";
        String movflags = "-movflags";
        String faststart = "faststart";
        Path outputPath = options.getOutputPath();
        String outputFilename = FilenameUtils.getBaseName(inputFile) + ".mp4";
        Path outputFile = FileService.buildOutputFile(outputPath, outputFilename, ".mp4");

        FileService.validateFiles(inputFile, outputFile);

        List<String> command = new ArrayList<>(Arrays.asList(FFMPEG, input, inputFile,
                mapChapters, mapChaptersValue,
                vf, vfValue,
                vcodec, vcodecValue,
                forceKeyFrames, expr,
                crf, crfValue, maxrate, maxrateValue,
                bufSize, bufSizeValue, pixFmt, pixFmtValue, acodec, acodecValue, ab, abValue,
                ditherMethod, ditherMethodValue, movflags, faststart,
                outputFile.toString()));
        CommandUtility.executeCommand(command);

        return outputFile;
    }
}
