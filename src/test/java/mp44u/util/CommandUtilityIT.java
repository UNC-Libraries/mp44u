package mp44u.util;

import static org.junit.jupiter.api.Assertions.assertThrows;

import mp44u.errors.CommandTimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * @author bbpennel
 */
public class CommandUtilityIT {
    @TempDir
    public Path tmpFolder;

    @Test
    public void testTimeout() {
        assertThrows(CommandTimeoutException.class,
                () -> CommandUtility.executeCommand(List.of("sleep", "5"), 1));
    }

    @Test
    public void testNoTimeout() {
        CommandUtility.executeCommand(List.of("sleep", "1"));
    }

    @Test
    public void testFfmpeg() {
        String inputPath = "src/test/resources/009.mp4";
        String outputPath = tmpFolder.resolve("output.mp4").toString();
        var command = Arrays.asList("ffmpeg", "-nostdin", "-nostats", "-i", inputPath,
                "-map_chapters", "-1", "-movflags", "faststart", "-c:s", "mov_text",
                "-vcodec", "libx264", "-crf", "22",
                "-vf", "yadif=0:-1:1,scale=trunc(oh*dar/2)*2:min(ih\\,720)",
                "-force_key_frames", "expr:gte(t,n_forced*2)", "-maxrate", "2M", "-bufsize", "4M",
                "-pix_fmt", "yuv420p", "-threads", "0",
                "-acodec", "aac", "-ab", "128k", "-ar", "44100", "-y",
                "-dither_method", "triangular", "-threads", "1", outputPath);
        assertThrows(CommandTimeoutException.class,
                () -> CommandUtility.executeCommand(command, 1));
    }
}
