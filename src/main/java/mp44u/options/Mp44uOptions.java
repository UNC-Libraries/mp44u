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

    @Option(names = {"-t", "-threads"},
            description = "Number of threads used for encoding",
            defaultValue = "0")
    private int threads;

    @Option(names = {"-T", "--timeout"},
            description = "Timeout in seconds for subcommands (default: ${DEFAULT-VALUE})",
            defaultValue = "${sys:mp44u.subcommand.timeout:-300}")
    private int subcommandTimeout;

    @Option(names = {"-sf", "--source-fmt"},
            description = "Override source file type detection.",
            defaultValue = "")
    private String sourceFormat;

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

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public int getSubcommandTimeout() {
        return subcommandTimeout;
    }

    public void setSubcommandTimeout(int subcommandTimeout) {
        this.subcommandTimeout = subcommandTimeout;
    }

    public String getSourceFormat() {
        return sourceFormat;
    }

    public void setSourceFormat(String sourceFormat) {
        this.sourceFormat = sourceFormat;
    }
}
