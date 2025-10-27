package mp44u.options;

import picocli.CommandLine.Option;

import java.nio.file.Path;

public class Mp44uOptions {
    @Option(names = {"-i", "--input-path"},
            required = true,
            description = "Required. Input path of the file to run commands on.")
    private Path inputPath;

    @Option(names = {"-o", "--output-path"},
            description = "Destination for converted images. You must set the output path manually, no default.")
    private Path outputPath;

    public Path getInputPath() {
        return inputPath;
    }

    public void setInputPath(Path inputPath) {
        this.inputPath = inputPath;
    }

    public Path getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(Path outputPath) {
        this.outputPath = outputPath;
    }
}
