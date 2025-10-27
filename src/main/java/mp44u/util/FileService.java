package mp44u.util;


import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for constructing files and file validation
 * @author krwong
 */
public class FileService {
    private static final Logger log = getLogger(FileService.class);

    private FileService() {}

    public static Path buildOutputFile(Path outputPath, String outputFilename, String extension)
            throws Exception {
        // if the output path is a directory
        if (Files.isDirectory(outputPath)) {
            return outputPath.resolve(outputFilename);
            // if the output path is a file
        } else if (Files.exists(outputPath.getParent())) {
            return Path.of(outputPath + extension);
        } else {
            throw new FileNotFoundException(outputPath + " does not exist.");
        }
    }

    public static void validateFiles(String inputFile, Path outputFile) throws Exception {
        if (inputFile.equals(outputFile.toString())) {
            throw new IllegalArgumentException("Input and output paths cannot be the same");
        }

        if (Files.exists(outputFile)) {
            throw new FileAlreadyExistsException("File already exists at " + outputFile);
        }
    }
}
