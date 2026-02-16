package mp44u;

import mp44u.errors.CommandTimeoutException;
import mp44u.options.Mp44uOptions;
import mp44u.services.AVInfoService;
import mp44u.services.AudioService;
import mp44u.services.VideoService;
import org.slf4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;

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

    private AudioService audioService;
    private VideoService videoService;
    private AVInfoService avInfoService;

    @Command(name = "audio",
            description = "Transcode audio file to m4a")
    public int audio(@Mixin Mp44uOptions options) throws Exception {
        try {
            initialize();
            audioService.convertOrCopyAudio(options);
            return 0;
        } catch (CommandTimeoutException e) {
            log.error("Audio command timed out", e);
            return 124;
        } catch (Exception e) {
            log.error("Failed to generate m4a file", e);
            return 1;
        }
    }

    @Command(name = "video",
            description = "Transcode video file to mp4")
    public int video(@Mixin Mp44uOptions options) throws Exception {
        try {
            initialize();
            videoService.convertOrCopyVideo(options);
            return 0;
        } catch (CommandTimeoutException e) {
            log.error("Video command timed out", e);
            return 124;
        } catch (Exception e) {
            log.error("Failed to generate mp4 file", e);
            return 1;
        }
    }

    private void initialize() throws IOException {
        avInfoService = new AVInfoService();
        audioService = new AudioService();
        audioService.setAvInfoService(avInfoService);
        videoService = new VideoService();
        videoService.setAvInfoService(avInfoService);
    }
}
