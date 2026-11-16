package tools;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

public class ReadTool {
    public static String readFileContents(String path) {
        Path filePath = Path.of(path);
        try {
            return Files.readString(filePath);
        } catch (IOException e) {
            return e.getMessage();
        }
    }
}
