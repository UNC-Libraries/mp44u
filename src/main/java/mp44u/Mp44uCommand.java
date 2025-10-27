package mp44u;

import mp44u.options.Mp44uOptions;
import mp44u.services.AudioService;
import mp44u.services.VideoService;
import org.slf4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.ParentCommand;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author krwong
 */
@Command(name = "mp44u",
        description = "")
public class Mp44uCommand {
    private static final Logger log = getLogger(Mp44uCommand.class);

    @ParentCommand
    private CLIMain parentCommand;

    private AudioService audioService = new AudioService();
    private VideoService videoService = new VideoService();

    @Command(name = "audio",
            description = "Transcode audio file to m4a")
    public int audio(@Mixin Mp44uOptions options) throws Exception {
        try {
            audioService.ffmpegConvertToM4a(options);
            return 0;
        } catch (Exception e) {
            log.error("Failed to generate m4a file", e);
            return 1;
        }
    }

    @Command(name = "video",
            description = "Transcode video file to mp4")
    public int video(@Mixin Mp44uOptions options) throws Exception {
        try {
            videoService.ffmpegConvertToMp4(options);
            return 0;
        } catch (Exception e) {
            log.error("Failed to generate mp4 file", e);
            return 1;
        }
    }
}
